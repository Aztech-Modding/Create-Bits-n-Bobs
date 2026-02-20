const fs = require('fs');
const path = require('path');
const ffmpeg = require('fluent-ffmpeg');
const ffmpegPath = require('ffmpeg-static');
const { Jimp, intToRGBA } = require('jimp');

// Configure ffmpeg
ffmpeg.setFfmpegPath(ffmpegPath);

const INPUT_FILE = path.join(__dirname, 'videoplayback.mp4');
const OUTPUT_DIR = path.join(__dirname, 'output');
const WIDTH = 128;
const HEIGHT = 128;

// Ensure output directory exists
if (!fs.existsSync(OUTPUT_DIR)) {
    fs.mkdirSync(OUTPUT_DIR);
}

// Helper to convert brightness to hex char (0 = white, f = black for ComputerCraft)
// Note: Standard CC colors:
// 0: White (f0f0f0)
// f: Black (111111)
const WHITE_CHAR = '0';
const BLACK_CHAR = 'f';

async function processVideo() {
    console.log('Starting processing...');
    
    // Create a temp directory for raw frames
    const tempDir = path.join(__dirname, 'temp_frames');
    if (!fs.existsSync(tempDir)) {
        fs.mkdirSync(tempDir);
    } else {
        // Clean temp dir
        fs.readdirSync(tempDir).forEach(file => fs.unlinkSync(path.join(tempDir, file)));
    }

    console.log('Extracting frames...');
    
    await new Promise((resolve, reject) => {
        ffmpeg(INPUT_FILE)
            .size(`${WIDTH}x${HEIGHT}`)
            .outputOptions('-r', '20') // Set framerate (e.g., 20fps) - adjust as needed
            .output(path.join(tempDir, 'frame-%d.png'))
            .on('end', resolve)
            .on('error', reject)
            .run();
    });

    console.log('Frames extracted. Processing to NFP...');

    const files = fs.readdirSync(tempDir)
        .filter(f => f.startsWith('frame-') && f.endsWith('.png'))
        .sort((a, b) => {
            const numA = parseInt(a.match(/frame-(\d+)\.png/)[1]);
            const numB = parseInt(b.match(/frame-(\d+)\.png/)[1]);
            return numA - numB;
        });

    for (let i = 0; i < files.length; i++) {
        const file = files[i];
        const frameNum = i + 1;
        const inputPath = path.join(tempDir, file);
        const outputPath = path.join(OUTPUT_DIR, `${frameNum}.nfp`);

        await processFrame(inputPath, outputPath);
        
        if (i % 100 === 0) {
            console.log(`Processed frame ${frameNum}/${files.length}`);
        }
    }

    // Cleanup temp dir
    // fs.rmdirSync(tempDir, { recursive: true });
    
    console.log('Done!');
}

async function processFrame(inputPath, outputPath) {
    const image = await Jimp.read(inputPath);
    
    // Determine "background" color based on corners
    // We'll sample the 4 corners (say, 10x10 blocks)
    let cornerBrightnessSum = 0;
    let cornerPixelCount = 0;
    const cornerSize = 16; // 128/8

    const width = image.bitmap.width;
    const height = image.bitmap.height;

    // Helper to get brightness (0-255)
    // Jimp pixels are RGBA. Brightness = (R+G+B)/3 or perceived
    const getBrightness = (x, y) => {
        const color = intToRGBA(image.getPixelColor(x, y));
        return (color.r + color.g + color.b) / 3;
    };

    // Sample corners
    // Top-Left
    for(let x=0; x<cornerSize; x++) {
        for(let y=0; y<cornerSize; y++) {
            cornerBrightnessSum += getBrightness(x, y);
            cornerPixelCount++;
        }
    }
    // Top-Right
    for(let x=width-cornerSize; x<width; x++) {
        for(let y=0; y<cornerSize; y++) {
            cornerBrightnessSum += getBrightness(x, y);
            cornerPixelCount++;
        }
    }
    // Bottom-Left
    for(let x=0; x<cornerSize; x++) {
        for(let y=height-cornerSize; y<height; y++) {
            cornerBrightnessSum += getBrightness(x, y);
            cornerPixelCount++;
        }
    }
    // Bottom-Right
    for(let x=width-cornerSize; x<width; x++) {
        for(let y=height-cornerSize; y<height; y++) {
            cornerBrightnessSum += getBrightness(x, y);
            cornerPixelCount++;
        }
    }

    const avgCornerBrightness = cornerBrightnessSum / cornerPixelCount;
    
    // The user says: "gray or anything inbetween should just be the dominant common color of the frame"
    // So if corners are bright (white background), we bias towards white.
    // If corners are dark (black background), we bias towards black.
    
    // Let's decide a threshold.
    // If background is White (high brightness), we want pixels to be White unless they are significantly Dark.
    // If background is Black (low brightness), we want pixels to be Black unless they are significantly Bright.
    
    // A simple approach is to use the avgCornerBrightness as the reference for "Background".
    // Anything "far" from Background becomes Foreground.
    // But since inputs are grayscale, "far" means crossing a threshold.
    
    // If avgCornerBrightness > 128 (White background), logic:
    //   Pixel < Threshold => Black (Foreground)
    //   Pixel > Threshold => White (Background)
    //   Threshold should be lower than avgCornerBrightness to "catch" grays as White.
    
    // If avgCornerBrightness < 128 (Black background), logic:
    //   Pixel > Threshold => White (Foreground)
    //   Pixel < Threshold => Black (Background)
    
    // Let's dynamic threshold. 
    // If avg_corner is high (e.g. 240), threshold might be 128.
    // If avg_corner is low (e.g. 10), threshold might be 128.
    
    // Actually, user said: "gray ... should just be the dominant common color".
    // This implies that Gray (128) becomes White if background is White, and Black if background is Black.
    // This simply means: Threshold = 128?
    // Wait, if avgCorner is White (255), we want Gray (128) to confirm to White.
    // So Threshold needs to be LOWER than 128 for Gray to be White? No.
    // If Threshold is 100. brightness 128 > 100 -> White.
    
    // If avgCorner is Black (0), we want Gray (128) to confirm to Black.
    // So Threshold needs to be HIGHER than 128.
    // If Threshold is 150. brightness 128 < 150 -> Black.
    
    // So Threshold should shift AWAY from the dominant color.
    // If dominant ~ 255 (White) -> Threshold shifts down (e.g. 80).
    // If dominant ~ 0 (Black) -> Threshold shifts up (e.g. 175).
    
    let threshold = 127;
    
    if (avgCornerBrightness > 128) {
        // Dominant is White. We want grays to be White.
        threshold = 80;
    } else {
        // Dominant is Black. We want grays to be Black.
        threshold = 175;
    }

    let nfpContent = '';

    for (let y = 0; y < height; y++) {
        for (let x = 0; x < width; x++) {
            const b = getBrightness(x, y);
            if (b >= threshold) {
                nfpContent += WHITE_CHAR;
            } else {
                nfpContent += BLACK_CHAR;
            }
        }
        nfpContent += '\n'; // Add newline at the end of each row
    }

    // Remove the very last newline to prevent empty line at end of file if that's an issue for NFP readers, 
    // but standard text files usually have it. ComputerCraft `paint` might not care, but better safe than sorry?
    // Actually, "Each row is terminated by a newline." implies the last row too.
    
    fs.writeFileSync(outputPath, nfpContent);
}

processVideo().catch(console.error);
