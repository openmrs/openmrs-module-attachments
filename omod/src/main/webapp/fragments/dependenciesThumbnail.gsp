<!-- Thumbnail directive dependencies -->
<%
  ui.includeJavascript("attachments", "directives/thumbnail.js")
  ui.includeJavascript("attachments", "services/attachmentService.js")
  ui.includeJavascript("attachments", "services/moduleUtils.js")
  ui.includeJavascript("attachments", "services/complexObsCacheService.js")
  ui.includeJavascript("uicommons", "ngDialog/ngDialog.js")
  ui.includeCss("uicommons", "ngDialog/ngDialog.min.css")
  ui.includeJavascript("attachments", "directives/modalImage.js")
  ui.includeCss("attachments", "icon.css")
  ui.includeJavascript("attachments", "date/moment.min.js")
  ui.includeJavascript("attachments", "image/exif.js")
  ui.includeJavascript("attachments", "image/angular-fix-image-orientation.js")
%>