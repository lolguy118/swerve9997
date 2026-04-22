// @ts-check
// markdownlint-cli2 configuration
// Ref: https://github.com/DavidAnson/markdownlint-cli2
//
// Rules:
//   - All default markdownlint rules enabled
//   - MD013 (line-length): max 140 chars; code blocks and tables exempt
//   - MD024 (no-duplicate-heading): siblings_only — duplicates allowed under different parents
//   - MD060 (table-column-style): compact — single space around cell content, including separators
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
      },
      MD024: {
         siblings_only: true
      },
      MD060: {
         style: "compact"
      }
   },
   ignores: [
      ".github/**",
      "WPILib-License.md"
   ]
};
