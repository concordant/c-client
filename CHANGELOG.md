# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]
### Added
### Changed
- Open an already opened object return the same reference
- Change key type of waitingPull from DeltaCRDT to CObjectUId
### Deprecated
### Removed
### Fixed
### Security

## [1.2.0] - 2021-05-06
### Added
- Add pull method in Collection

## [1.1.7] - 2021-04-16
### Changed
- Limit the frequency of get requests
- Group updates of the same transaction
- Update status

## [1.1.6] - 2021-03-25
### Added
- Add more unit tests
- Add status file
### Changed
- UUIds are hexadecimal
