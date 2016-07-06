<!-- Copied from http://www.w3schools.com/howto/howto_css_modal_images.asp -->
<style>

/* The Modal (background) */
.vdui_modal-image-background {
  position: fixed; /* Stay in place */
  z-index: 10; /* Sit on top, in particular of DropzoneJS's thumbnail */
  padding-top: 100px; /* Location of the box */
  left: 0;
  top: 0;
  width: 100%; /* Full width */
  height: 100%; /* Full height */
  overflow: auto; /* Enable scroll if needed */
  background-color: rgb(0,0,0); /* Fallback color */
  background-color: rgba(0,0,0,0.8); /* Black w/ opacity */
}

/* Modal Content (Image) */
.vdui_modal-image-content {
  margin: auto;
  display: block;
  max-height: 80%;
}

/* Caption of Modal Image (Image Text) - Same Width as the Image */
#vdui_modal-image-caption {
  margin: auto;
  display: block;
  width: 80%;
  max-width: 700px;
  text-align: center;
  color: #ccc;
  padding: 10px 0;
  height: 150px;
}

/* Add Animation - Zoom in the Modal */
.vdui_modal-image-content, #vdui_modal-image-caption { 
  -webkit-animation-name: zoom;
  -webkit-animation-duration: 0.6s;
  animation-name: zoom;
  animation-duration: 0.6s;
}

@-webkit-keyframes zoom {
  from {-webkit-transform:scale(0)} 
  to {-webkit-transform:scale(1)}
}

@keyframes zoom {
  from {transform:scale(0)} 
  to {transform:scale(1)}
}

</style>

<!-- The Modal -->
<div ng-if="config" class="vdui_modal-image-background" ng-click="hide()">
  
  <!-- Modal Content (The Image) -->
  <img class="vdui_modal-image-content" ng-src="data:{{config.mimeType}};base64,{{config.bytes}}">

  <!-- Modal Caption (Image Text) -->
  <div id="vdui_modal-image-caption">{{config.caption}}</div>
</div>