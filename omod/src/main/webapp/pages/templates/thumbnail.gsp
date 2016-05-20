<style>

	/* Credits to: https://designshack.net/articles/css/how-to-build-a-responsive-thumbnail-gallery/ */

	/* MEDIA QUERIES*/
	@media only screen and (max-width : 940px),
	only screen and (max-device-width : 940px){
		.vdui_thumbnailContainer {width: 21%;}
	}

	@media only screen and (max-width : 720px),
	only screen and (max-device-width : 720px){
		.vdui_thumbnailContainer {width: 29.33333%;}
	}

	@media only screen and (max-width : 530px),
	only screen and (max-device-width : 530px){
		.vdui_thumbnailContainer {width: 46%;}
	}

	@media only screen and (max-width : 320px),
	only screen and (max-device-width : 320px){
		.vdui_thumbnailContainer {width: 96%;}
		.vdui_thumbnailContainer img {width: 96%;}
	}

	.vdui_thumbnailContainer {
		float: left;

		width: 16%;	/* Example for 5 thumbnails: 100/5 - the left and right margins below */
	 	margin:  2% 2% 50px 2%; 

	 	height: 130px; /* Controls the height of the whole thumbnail */
	}

	.vdui_thumbnailContainer p {
		font-size: smaller;
	}

	.vdui_thumbnailContainer p:hover {
		/*background-color: #e0e0e0;*/
		background-color: #F5F5F5;

		font-style: italic;
		
		-webkit-border-radius: 3px;
		-moz-border-radius: 3px;
		border-radius: 3px;
		padding: 5px;
	}

	.vdui_thumbnailImage {
		position: relative;
		height: 110px; /* Controls the height of the image */
	}

	.vdui_thumbnailContainer img {
		max-width: 100%;
		-webkit-border-radius: 5px;
		-moz-border-radius: 5px;
		border-radius: 5px;

		max-height: 110px; /* Controls the height of the image */
	}

	.vdui_thumbnailCaption {
		margin-top: 10px;
		position: relative;
	}

	.vdui_thumbnailImage a {
		position: absolute;
		width: 80%;
		height: auto;
    left: 0;
    top: 0;	/* Controls how low is placed the clickable image */
	}

	.vdui_thumbnailImage i {
		position: absolute;
    left: 0;
    top: 0;

    font-size: 300%
	}

	.vdui_editMode img {
		-moz-opacity: 0.20;
		opacity:.20;
		filter: alpha(opacity=20);
	}

	.vdui_thumbnailContainer i {
		cursor: pointer;
	}

</style>

<div class="vdui_thumbnailContainer" ng-class="editModeCss">

	<div class="vdui_thumbnailImage">
	  <a target="_blank" href="{{config.afterUrl}}{{obs.uuid}}"><img ng-src="{{config.url}}{{obs.uuid}}" alt=""></img></a>
	  <i ng-show="editMode" class="icon-trash" ng-click="delete()"></i>
	</div>

	<div class="vdui_thumbnailCaption">
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