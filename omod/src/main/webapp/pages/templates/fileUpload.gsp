<style>

  .vdui_file-upload-container {
    padding-left: 20px;
    height: 200px;
  }

  .vdui_caption-element textarea {
    width: 100%;
    -webkit-box-sizing: border-box; /* Safari/Chrome, other WebKit */
    -moz-box-sizing: border-box;    /* Firefox, other Gecko */
    box-sizing: border-box;         /* Opera/IE 8+ */
  }

  .vdui_upload-container {
    height: 180px;
  }

  .vdui_upload-element {
    float: left;
  }

  .vdui_upload-element.vdui_dropzone-element {
    width: 30%;
    height: 75%;
  }

  .vdui_upload-element.vdui_caption-element {
    width: 55%;
    margin-left: 2%;
  }

  .dropzone {
    position: center;
    border: 2px dotted #888;
    -webkit-border-radius: 10px;
    -moz-border-radius: 10px;
    border-radius: 10px;
    min-height: 0px;
    height: 100%;
    text-align: center;
  }

  .dropzone .dz-preview {
    position: absolute;
    margin: auto;
    left: 0;
    right:0;
    bottom: 0;
  }

  .dropzone .dz-preview .dz-image {
    z-index: 0;
  }

  .dropzone .dz-preview .dz-progress {
    z-index: 0;
  }

</style>

<div ng-show="visitUuid" class="vdui_file-upload-container" ng-init="init()">
  <div class="vdui_upload-container">
    <div class="vdui_upload-element vdui_dropzone-element">
      <h3>${ui.message("visitdocumentsui.visitdocumentspage.fileTitle")}</h3>
      <form action="" dropzone-directive="dropzoneConfig" class="dropzone" id="visit-documents-dropzone">
        <div class="dz-error-message"><span data-dz-errormessage></span></div>
        <div class="dz-default dz-message">${ui.message("visitdocumentsui.dropzone.innerlabel")}</div>
      </form>
    </div>
    <div class="vdui_upload-element vdui_caption-element">
      <h3>${ui.message("visitdocumentsui.visitdocumentspage.commentTitle")}</h3>
      <textarea ng-model="fileCaption"></textarea>
      <span class="right" style="margin-top: 4%;">
        <button class="confirm" ng-click="uploadFile()" ng-disabled="isUploadBtnDisabled()">${ui.message("visitdocumentsui.visitdocumentspage.uploadButton")}</button>
        <button ng-click="clearForms()">${ui.message("visitdocumentsui.visitdocumentspage.clearFormsButton")}</button>
      </span>
    </div>
  </div>
</div>