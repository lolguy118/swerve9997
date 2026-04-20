// @ts-check
// markdownlint-cli2 configuration
// Ref: https://github.com/DavidAnson/markdownlint-cli2
//
// Rules:
//   - All default markdownlint rules enabled
//   - MD013 (line-length): max 140 chars; code blocks and tables exempt
//   - .github/ directory excluded (issue/PR templates may exceed limits)

/** @type {import("markdownlint-cli2").Options} */
export default {
   config: {
      default: true,
      MD013: {
         line_length: 140,
         code_blocks: false,
         tables: false,
         headings: false
      }
   },
   ignores: [
      ".github/**"
   ]
};
