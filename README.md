# OpenMRS Attachments Module (backend)
The **Attachments** module brings a dedicated Java and web API to manage patient file attachments in OpenMRS.
<br>It encompasses files uploaded elsewhere within OpenMRS as long as they are saved as [complex obs](https://wiki.openmrs.org/display/docs/Creating+Complex+Observations+and+Concepts).

In a nutshell the Attachments module is a '**complex obs management API module**' whose Java API is designed to be extended, through new version of the module, to support further content types and concept complex coded obs.

### Content is handled based on its MIME type
The Attachments module is designed to handle content (or MIME) types on an ad-hoc basis.
For example images (files with `image/*` content types) are provided a custom handler that saves them alongside their thumbnails. This is intended for frontend implementations that need to load many images efficiently (in galleries for instance).

### _Not-yet-handled_ content types
When a content type is not provided a bespoke handling mechanism, it can still be accessed generically as it would be on any 'drive' or storage.

### How to try it out?
Build the master branch and install the built OMOD to your instance running the OpenMRS with the REST web-services module installed.
```
git clone https://github.com/openmrs/openmrs-module-attachments/tree/master
cd openmrs-module-attachments
mvn clean package
```
##### Runtime requirements & compatibility
* [Core 2.3.0 and beyond](https://github.com/openmrs/openmrs-core)
* [OpenMRS REST Web Services module 2.33.0 and beyond](https://github.com/openmrs/openmrs-module-webservices.rest)

----

### Releases notes
#### Version 3.0.0
⚠️ Breaking changes ⚠️
* This module no longer supports the 2.x UI Framework functionalities. I.e. it has become headless with only REST endpoints for the management of attachments. For support of the removed features please use versions 2.6.0 version and below.
