<style>

	/* Credits to: https://designshack.net/articles/css/how-to-build-a-responsive-thumbnail-gallery/ */

	/* MEDIA QUERIES*/
	@media only screen and (max-width : 940px),
	only screen and (max-device-width : 940px){
		.vdui_thumbnail-container {width: 21%;}
	}

	@media only screen and (max-width : 720px),
	only screen and (max-device-width : 720px){
		.vdui_thumbnail-container {width: 29.33333%;}
	}

	@media only screen and (max-width : 530px),
	only screen and (max-device-width : 530px){
		.vdui_thumbnail-container {width: 46%;}
	}

	@media only screen and (max-width : 320px),
	only screen and (max-device-width : 320px){
		.vdui_thumbnail-container {width: 96%;}
		.vdui_thumbnail-container img {width: 96%;}
	}

	.vdui_thumbnail-container {
		float: left;

		width: 16%;	/* Example for 5 thumbnails: 100/5 - the left and right margins below */
	 	margin:  2% 2% 30px 2%;

	 	height: 130px; /* Controls the height of the whole thumbnail */
	}

	.vdui_thumbnail-container p {
		font-size: smaller;
	}

	.vdui_thumbnail-container p:hover {
		background-color: #F5F5F5;

		font-style: italic;
		
		-webkit-border-radius: 3px;
		-moz-border-radius: 3px;
		border-radius: 3px;
		padding: 5px;
	}

	.vdui_thumbnail-image-section {
		position: relative;
		height: 110px; /* Controls the height of the image */
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
		margin-top: 10px;
		position: relative;
	}

	.vdui_thumbnail-image-section a {
		position: absolute;
		width: 80%;
		height: auto;
    left: 0;
    top: 0;	/* Controls how low is placed the clickable image */
	}

	.vdui_thumbnail-image-section i {
		position: absolute;
    left: 0;
    top: 0;

    font-size: 300%
	}

	.vdui_thumbnail-edit-mode img {
		-moz-opacity: 0.20;
		opacity:.20;
		filter: alpha(opacity=20);
	}

	.vdui_thumbnail-edit-mode img:hover {
		cursor: auto;
	}

	.vdui_thumbnail-container i {
		cursor: pointer;
	}

	.vdui_side {
		display: inline-block;
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

<vdui-modal-image></vdui-modal-image>

<div ng-show="active" class="vdui_thumbnail-container" ng-class="getEditModeCss()">

	<div class="vdui_thumbnail-image-section">
	  <img ng-click="!editMode && display()" ng-src="{{config.url}}{{obs.uuid}}" alt=""></img>
	  <i ng-show="editMode" class="icon-trash" ng-click="confirmDelete()"></i>
	</div>

	<div class="vdui_thumbnail-caption-section">
		<div ng-hide="editMode" ng-click="toggleEditMode(true)">
			<i ng-hide="obs.comment" class="icon-tag vdui_side"></i>
			<p ng-show="obs.comment" class="vdui_side">{{obs.comment}}</p>
		</div>
		<div ng-show="editMode" vdui-escape-key-down="toggleEditMode(false)">
	    <!-- <input vdui-focus-on="vdui_event_editMode" ng-model="newCaption" class="left" type="text" vdui-enter-key-down="saveCaption()"/> -->
	    <input ng-model="newCaption" class="left" type="text" vdui-enter-key-down="saveCaption()"/>
	    <span class="right">
	      <i class="icon-ok" ng-click="saveCaption()"></i>
	      <i class="icon-remove" ng-click="toggleEditMode(false)"></i>
	    </span>
	  </div>
	  <i ng-show="loading" class="icon-spinner icon-spin" style="margin-left: 10px;"></i>
  </div>

</div>