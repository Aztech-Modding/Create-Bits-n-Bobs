# Agent code guidelines (Common pitfalls)

## Code quality guidelines

- No comments please, unless absolutely necessary (i.e. explaining the *why* behind a weird non-obvious technique). If
  you need to explain the what of something, put it in a javadoc
- Avoid "comment blocking" where you segment a method with comments like `// Process X`, `// Process Y`. If you need to
  do this, consider splitting the method into sub methods instead, same goes for classes, dont block methods by
  regioning them, just split into classes to reduce complexity (though most of the time you can omit them)
- NEVER do a final class, and never do a private noargs constructor. I find this code unnecessarily and code smelly.
- Encapsulate complex data structures or structures that require specific access patterns in their own class to abstract
  away complexity and improve readability
- Always use `this.` never use qualified names, never use `var`
- Prioritize creating more modules over adding functionality to existing ones
- ALWAYS perform a post-implementation code review. Ask yourself whether this structure would hold up in a meeting.
  Think pros and cons of this approach, and quick wins to improve readability

## Agent MCP use guidelines (IMPORTANT)

- Always use the intellij ide_diagnostics tool after writing code. Special attention should be paid to:
    - Class can be record
    - Method X can be replaced with Y, where Y is a more concise way (Except for streams, which should be avoided in
      ticking code)
- Always use the java_code_quality mcp (if enabled, notify if it wasnt available). Assess code complexity and use the
  structure quality tool, these are purpose built so i strongly suggest you follow advice when reasonable (Ignore
  inspections for files you havent touched). Notify me of any instance of ignoring the tool or bad structure misses so i
  can improve it in future.

## Leave stuff better than you found it

For any highly complex or overly coupled classes, you must not make anything worse or be complicit in bad code that you
edit in any way. You must dispatch a subagent to improve the score. I will not accept any PRs that make the code
complexity worse, and I will not accept any change that do not make an effort to improve the structure quality as a
whole. Always consider how you could abstract your systems to make them useful for the next agent.