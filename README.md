# Attachments
The **Attachments** module brings to the OpenMRS Reference Application a central place to manage file attachments.
<br>Additionally it encompasses files uploaded elsewhere within OpenMRS as long as they are saved as [complex obs](https://wiki.openmrs.org/display/docs/Creating+Complex+Observations+and+Concepts).

### A central place for attachments management
In a nutshell Attachments is a '**complex obs management** module' ready to be extended to further content types and concept complex coded obs.

### Content is handled based on its MIME type
For example images are subject to a custom implementation that saves them alongside their thumbnails for faster gallery loading. Moreover a modal viewing applies for most `image/*` content types:

New UI behaviours and backend handlers can be added for further content types.
<br>You are a developer and you want to contribute? [Just get in touch.](#get-in-touch)

### _Not-yet-handled_ content types
When a content type is not yet provided a bespoke implementation, it can still be accessed generically as on a 'drive':

### How to try it out?
Build the master branch and install the built OMOD to your instance running the OpenMRS with the REST web-services module installed.
```
git clone https://github.com/openmrs/openmrs-module-attachments/tree/master
cd openmrs-module-attachments
mvn clean install
```
##### Runtime requirements & compatibility
* [Core 2.3.0 and beyond](https://github.com/openmrs/openmrs-core)
* [Openmrs Webservices.rest module 2.33.0 and beyond](https://github.com/openmrs/openmrs-module-webservices.rest)

<br>[Get in touch](#get-in-touch) if this is a blocker and that you cannot obtain a compatible version of Core Apps.
* Google Chrome 51+

### Get in touch
Find us on [OpenMRS Talk](https://talk.openmrs.org/): sign up, start a conversation and ping us with the mentions starting with @mks.. in your message.

----

### Releases notes

#### Version 3.0.0
##### New features
* This module no longer supports the 2.x UI framework functionalities, i.e. it is headless with only REST endpoints for the management of attachments. For support of the removed features please refer to the 2.x branch of this project.
