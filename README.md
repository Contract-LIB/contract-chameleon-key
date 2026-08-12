# `contract-chameleon-key`

This repository contains the [`KeY`](https://github.com/KeYProject/key) adapters,
for [contract-chameleon](https://github.com/Contract-LIB/contract-chameleon).

## Available Adapters

- Export Adapters
  - `key-provider`
  - `key-applicant`
  - `key-universe` (work in progress)
- Import Adapters
  - `key-import`
- Checker Adapters
  - `key-universe-check` (work in progress)

## Project Setup

Just run `./build.sh`,
which sets up the necessary git submodules,
and publishes the required dependencies locally.

## JavaDoc of Dependencies

### From Modules

For each module the `JavaDoc` can be found in
`<module>/build/docs/javadoc/org/contract_lib/contract_chameleon/package-summary.html`.

### From `jmlparser`

```sh
# build Javadoc
./mvnw javadoc:javadoc
```

The `JavaDoc` can be found in for the different packages: `<jmlparser-module>/javaparser-core/target/reports/apidocs/index.html`
