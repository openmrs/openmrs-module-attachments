# Visit Documents UI
The **Visit Documents UI** module (VDUI) brings to the OpenMRS Reference Application a central place to view & manage patient documents.
<br>Additionally it encompasses documents uploaded elsewhere within OpenMRS as long as they are saved as [complex obs](https://wiki.openmrs.org/display/docs/Creating+Complex+Observations+and+Concepts).

### A central place for patient documents management
In a nutshell VDUI is a documents gallery with an upload widget.
![alt tag](readme/vdui_mainpage.png)

### Consistent dashboards integration
VDUI both integrates on the Clinician Facing Patient Dashboard and on the Visits & Encounters Patient Dashboard.
![alt tag](readme/vdui_cfdashboard.png)

### Content is handled based on its MIME type
For example images are subject to a custom implementation that saves them alongside their thumbnails for faster gallery loading. Moreover a modal viewing applies for most `image/*` content types:
![alt tag](readme/vdui_imagemodal.png)

New UI behaviours and backend handlers can be added for further content types.
<br>You are a developer and you want to contribute? [Just get in touch.](#get-in-touch)

### _Not-yet-handled_ content types
When a content type is not yet provided a bespoke implementation, it can still be accessed generically as on a 'drive':
![alt tag](readme/vdui_galleryzipfile.png)

### How to try it out?
Build the master branch and install the built OMOD to your instance running the OpenMRS Reference Application:
```
git clone https://github.com/mekomsolutions/openmrs-module-visitdocumentsui/tree/master
cd openmrs-module-visitdocumentsui
mvn clean install
```
##### Runtime requirements & compatibility
* Core 1.10.2 to Platform 2.0.0
* Reference Application distribution 2.3+
* Core Apps module fixed with [RA-1155](https://issues.openmrs.org/browse/RA-1155)
<br>RA-1155 brings in the thumbnails on the Visits & Encounters Patient Dashboard.
<br>[Get in touch](#get-in-touch) if this is a blocker and that you cannot obtain a compatible version of Core Apps.
* Google Chrome 51+

### Quick facts
* VDUI is in fact a '**complex obs management UI**' ready to be extended to further content types and concept complex coded obs.

* Although every _patient_ document can be handled, **VDUI's interface only allows to add documents within a visit**. This complies to the usual Ref App's workflow where medical documentation happens within visits through medical encounters.

### Get in touch
Find us on [OpenMRS Talk](https://talk.openmrs.org/): sign up, start a conversation and ping us with the mentions starting with @mks.. in your message.

----

### Releases notes

#### Version 1.2
##### New features
* Allows to add documents to closed visits.
* Reorients images based on their EXIF metadata.

##### Bugfixes
* Cannot purge documents with missing underlying complex data file (see [TRUNK-5077](https://issues.openmrs.org/browse/TRUNK-5077)).
This requires Core 1.10.5, 1.11.8, 1.12.1 or 2.0.5 at runtime.
* Upload fails for portrait images with some EXIF metadata.
* Thumbnails are not reloaded when clicking on 'show/hide details' multiple times on the patient dashboard.
* Date/time stamps use incorrect locale on the patient dashboard.

#### Version 1.1
##### New features
* Cross Core/Platform compatibility from 10.10.2 to 2.0.0.

##### Bugfixes
* Ensure unique naming of uploaded files because of [TRUNK-5093](https://issues.openmrs.org/browse/TRUNK-5093).
* Cannot delete visit document after editing the caption (without the need to reload the page.)

#### Version 1.0
##### New features
* Supports specific implementations for image and PDF files.
* Supports generic support for all other MIME types.
* Integrates with the Reference Application clinician facing dashboard.
* Integrates with the Reference Application patient Dashboard.
* Allows to use webcam to capture images on desktop computers.