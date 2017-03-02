// This file was created in order to declare all the angular modules. This way we can ensure that all the
// dependency problems between controllers, directives and services will be resolved.

angular.module('vdui.page.main', ['obsService', 'session', 'vdui.widget.fileUpload', 'vdui.widget.gallery', 'vdui.widget.thumbnail']);
angular.module('vdui.fragment.dashboardWidget', ['obsService', 'vdui.widget.gallery', 'vdui.widget.thumbnail']);
angular.module('vdui.fragment.encounterTemplate', ['vdui.widget.complexObsEncounter']);

angular.module('vdui.widget.modalImage', ['vdui.service.moduleUtils']);
angular.module('vdui.widget.complexObsEncounter', ['obsService', 'vdui.widget.gallery', 'vdui.widget.thumbnail', 'vdui.widget.modalImage']);
angular.module('vdui.widget.gallery', ['vdui.service.configService', 'vdui.service.moduleUtils']);
angular.module('vdui.widget.modalWebcam', ['vdui.service.moduleUtils']);
angular.module('vdui.widget.fileUpload', ['vdui.widget.modalWebcam', 'vdui.service.moduleUtils']);
angular.module('vdui.widget.thumbnail', ['vdui.service.visitDocumentService', 'vdui.service.complexObsCacheService', 'ngDialog', 'vdui.widget.modalImage','vdui.service.moduleUtils', 'cp.ng.fix-image-orientation'])

angular.module('vdui.service.configService', ['vdui.service.moduleUtils']);
angular.module('vdui.service.moduleUtils', []);
angular.module('vdui.service.visitDocumentService', ['ngResource', 'uicommons.common']);
angular.module('vdui.service.complexObsCacheService', ['vdui.service.moduleUtils']);
