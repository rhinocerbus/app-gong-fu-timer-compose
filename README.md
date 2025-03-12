# app-composite-build-template

## Steps for fully converting from template
### package changes
find/replace/refactor "movies_sample_compose" or "movies-sample-compose"

## adding private libraries
find files with tag:
// composite build config
or values in gradle files with "template-fill"
ex: applicationId = "com.whatever.template-fill"

### add as git submodule (ex: through sourcetree)
todo - steps, commands
### add to gradle
todo - steps, files, etc.
settings.gradle.kts
includeBuild("lib-retrofit-moshi")