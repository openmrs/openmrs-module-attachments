<style>

.vdui_thumbnails-container {
  width: 100%;
  overflow: hidden;
  padding: 0 0 20px 0;
}

</style>

<div class="vdui_thumbnails-container">
  <i ng-hide="obsArray && config" class="icon-spinner icon-spin icon-2x" style="margin-left: 10px;"></i>
  <div ng-show="obsArray && config">
    <div ng-show="obsArray.length">
      <vdui-thumbnail ng-repeat="obs in obsArray" obs="obs" config="config"></vdui-thumbnail>
    </div>
    <div ng-hide="obsArray.length">
      ${ui.message("visitdocumentsui.visitdocumentspage.noDocuments")}
    </div>
  </div>
</div>