package com.kipti.bnb.mixin;

import com.kipti.bnb.content.kinetics.cogwheel_chain.riding.CogwheelChainSkyhookRenderer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidModel.class)
public class HumanoidModelMixin {

	@Inject(method = "setupAnim", at = @At("HEAD"))
	private void bitsnbobs$beforeSetupAnim(LivingEntity entity, float limbSwing, float limbSwingAmount,
										   float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
		if (entity instanceof Player player)
			CogwheelChainSkyhookRenderer.beforeSetupAnim(player, (HumanoidModel<?>) (Object) this);
	}

	@Inject(method = "setupAnim", at = @At("TAIL"))
	private void bitsnbobs$afterSetupAnim(LivingEntity entity, float limbSwing, float limbSwingAmount,
										  float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
		if (entity instanceof Player player)
			CogwheelChainSkyhookRenderer.afterSetupAnim(player, (HumanoidModel<?>) (Object) this);
	}
}
