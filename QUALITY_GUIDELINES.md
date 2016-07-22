This page lists criteria that code, commits and pull requests should fulfill.

## Commits and Pull Requests
 1. Commit messages should have a short message and a description. The description should explain: (a) what's changed. (b) What problem is fixed or what feature does work now (c) What alternative solutions were discarded and why.
 2. Commit messages should refer to the Github Issue, if there is one
 3. Pull Requests must have a cleaned-up sequence of commits. Do not put unrelated changes in the same commit. Squash commits if they contribute to the same logical change.
 4. Commits must be reviewable by a human. Do not re-format code that's otherwise not changed. Make sure commits don't become too large.
 5. Avoid putting generated source code and hand-written source code in the same commit, except for small changes. 
 6. Changes must have unit tests
 7. Good changes are the ones that do not cause, or even minimize future effort, e.g. bugfixes and follow-up feature requests. 

## Source Code
 1. Readable and understandable source code is good source code.
 2. Keep the source code well-formatted, preferably with the Eclipse formatter.
 3. Put high-level comments in the code to explain your intention, solved use cases, design decisions, rejected solutions, etc. Do *not* duplicate what is obvious from the code itself. People will read code + documentation, therefore documentation does not need to be self-contained. 

## API
 1. We must not break binary backwards compatibility of runtime artifacts. That are the artifacts an Xtext DSL user has installed. This does *not* include `xtext.generator` and test utilities.
 2. If we introduce breaking changes without violating the first rule, the DSL developer should be guided as good as possible through the migration efforts. E.g. through comments on `@Deprecated` annotations or log messages.
 3. We should be careful about what we make API, because we will need to maintain it for a long time
