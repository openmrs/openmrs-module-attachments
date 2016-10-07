<style>

  .vdui_header {
    position: relative;
    height: 20px;
  }

  .vdui_thumbnail-container {
    float: left;

    width: 130px;
    margin: 2% 2% 30px 2%;

    height: 160px; /* Controls the height of the whole thumbnail */
  }

  .vdui_date-time {
    font-size: 65%;
    font-weight: bold;
    color: #888;
  }

  .vdui_thumbnail-caption-section p {
    font-size: 90%;
  } 

  .vdui_editable p:hover {
    max-width: 130px;
    background-color: #F5F5F5;
    color: #F26522;
    font-weight: bold;
    
    -webkit-border-radius: 3px;
    -moz-border-radius: 3px;
    border-radius: 3px;
    padding: 3px;
    cursor: pointer;
  }

  .vdui_thumbnail-image-section {
    position: relative;
    height: 110px; /* Controls the height of the image */
  }

  .vdui_thumbnail-frame {
    position: relative;
    height: 110px; /* Controls the height */

    border: 3px solid #F4F4F4;

    -webkit-border-radius: 5px;
    -moz-border-radius: 5px;
    border-radius: 5px;
    padding: 5px;
  }

  .vdui_thumbnail-frame i {
    position: absolute;
    top: -2px;
    left: -2px;
    font-size: 5em !important; /* because of the CF dashboard */
  }

  .vdui_thumbnail-frame span {
    position: absolute;
    bottom: 10%;
    left: 10%;

    font-size: smaller;
  }

  .vdui_thumbnail-container img {
    max-width: 100%;
    -webkit-border-radius: 5px;
    -moz-border-radius: 5px;
    border-radius: 5px;

    max-height: 110px; /* Controls the height of the image */
  }

  .vdui_thumbnail-container img:hover {
    cursor: pointer;
  }

  .vdui_thumbnail-caption-section {
    margin-top: 20px;
    position: relative;
  }

  .vdui_thumbnail-caption-section input  {
    position: relative;
    max-width: 130px;
    font-size: 90%;
  }

  .vdui_file-name {
    overflow: hidden;
    color: lightgrey;
    font-size: 0.7em !important;
  }

  .vdui_icon-trash {
    position: absolute;
    left: 0;
    top: 0;

    font-size: 250%
  }

  .vdui_thumbnail-edit-mode .vdui_opacity-changeable {
    -moz-opacity: 0.15;
    opacity:.15;
    filter: alpha(opacity=15);
  }

  .vdui_thumbnail-edit-mode img:hover {
    cursor: auto;
  }

  .vdui_click-pointer {
    cursor: pointer;
  }

  .vdui_thumbnail-extension {
    font-weight: bold;
    font-family: OpenSansBold;
    text-align: -webkit-center;
    width: 45%;
  }

</style>
<script type="text/ng-template" id="vdui_thumbnail-confirm-dialog">
  <div>
    <div class="dialog-header">
      <h3>${ ui.message("visitdocumentsui.visitdocumentspage.delete.title") }</h3>
    </div>
    <div class="dialog-content">
      <input type="hidden" id="encounterId" value=""/>
      <ul>
        <li class="info">
          <span>${ ui.message("visitdocumentsui.visitdocumentspage.delete.confirm") }</span>
        </li>
      </ul>
      <button class="confirm right" ng-click="confirm()">${ ui.message("coreapps.yes") }
        <i ng-show="showSpinner" class="icon-spinner icon-spin icon-2x" style="margin-left: 10px;"></i>
      </button>
      <button class="cancel" ng-click="closeThisDialog()">${ ui.message("coreapps.no") }</button>
    </div>
  </div>
</script>

<vdui-modal-image config="imageConfig"></vdui-modal-image>

<div ng-if="active" class="vdui_thumbnail-container" ng-class="getEditModeCss()" ng-init="init()">
  <div class="vdui_header">
    <p class="vdui_date-time left">
      <time datetime="{{obs.obsDatetime}}">{{getPrettyDate()}}</time>
    </p>
  </div>

  <div class="vdui_thumbnail-image-section vdui_click-pointer" ng-click="!editMode && displayContent()">
    <div class="vdui_opacity-changeable vdui_thumbnail-frame">
      <i ng-hide="::obs.contentFamily" class="icon-file"></i>
      <div ng-show="::obs.contentFamily">
        <div ng-bind-html="::iconHtml"></div>
      </div>
    </div>
    <i ng-show="editMode" class="icon-trash vdui_icon-trash vdui_click-pointer" ng-click="confirmDelete()"></i>
  </div>

  <div class="vdui_thumbnail-caption-section" ng-class="canEdit() ? 'vdui_editable' : ''">
    <div ng-hide="editMode" ng-click="toggleEditMode(true)">
      <!-- <i ng-hide="obs.comment" class="icon-tag vdui_click-pointer vdui_side"></i> -->
      <div ng-hide="obs.comment" class="vdui_file-name">
        <p>{{obs.fileName}}</p>
      </div>
      <p ng-show="obs.comment">{{obs.comment}}</p>
    </div>
    <div ng-show="editMode" vdui-escape-key-down="toggleEditMode(false)">
      <input ng-model="typedText.newCaption" class="left" type="text" placeholder="${ui.message('visitdocumentsui.misc.label.enterCaption')}" vdui-enter-key-down="saveCaption()"/>
      <span class="right">
        <i class="icon-ok vdui_click-pointer" ng-click="saveCaption()"></i>
        <i class="icon-remove vdui_click-pointer" ng-click="toggleEditMode(false)"></i>
      </span>
    </div>
    <i ng-show="loading" class="icon-spinner icon-spin" style="margin-left: 10px;"></i>
  </div>
</div>