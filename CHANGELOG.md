# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

- Added
- Changed
- Deprecated
- Removed
- Fixed
- Security

## [1.3.3] - 2022-09-09

- Changed
  - Open Source the code
  - change Gitlab links to github
  - changelod packed format
  - 2022 License
  - dev documentation

## [1.3.2] - 2021-12-03

- Changed
  - Remove the hard coded port and use the customizable one

## [1.3.1] - 2021-12-02

- Fixed
  - Bug fix on WebSocket path addition

## [1.3.0] - 2021-07-20

- Added
  - Add (un)subscription request
  - Add explicit get method
  - Register a service worker (if available) that manages update messages
  - Automatically open a WebSocket connection if ServiceWorker is not available
  - Disconnect the WebSocket when closing the connection to the database
  - Add a getter for openedCollections in Session
  - Add multiple handlers per object
- Changed
  - Use NotificationHandler to notify the app when an update arrives
  - Automatic subscription when opening a collection
  - Change key type of waitingPull from DeltaCRDT to CObjectUId
  - Remove implicit get when performing onRead
  - Remove the limit of frequency of get requests
  - Open an already opened object in a different mode raises an exception

## [1.2.0] - 2021-05-06

- Added
  - Add pull method in Collection

## [1.1.7] - 2021-04-16

- Changed
  - Limit the frequency of get requests
  - Group updates of the same transaction
  - Update STATUS.md

## [1.1.6] - 2021-03-25

- Added
  - Add new unit tests
  - Add STATUS.md file
- Changed
  - UUIds are hexadecimal
