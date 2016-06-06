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
	 	margin:  2% 2% 50px 2%;

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

</style>

<div class="vdui_thumbnail-container" ng-class="editModeCss">

	<div class="vdui_thumbnail-image-section">
	  <img ng-click="show()" ng-src="{{config.url}}{{obs.uuid}}" alt=""></img>
	  <i ng-show="editMode" class="icon-trash" ng-click="delete()"></i>
	</div>

	<div class="vdui_thumbnail-caption-section">
		<p ng-show="!editMode" ng-click="toggleEditMode(true)">{{obs.comment}}</p>
		<div ng-show="editMode">
	    <input ng-model="newCaption" class="left" type="text"/>
	    <span class="right">
	      <i class="icon-ok" ng-click="saveCaption()"></i>
	      <i class="icon-remove" ng-click="toggleEditMode(false)"></i>
	    </span>
	  </div>
  </div>

</div>