var module = new function() {

	this.family = {};
  this.family.IMAGE = "IMAGE";
  this.family.PDF = "PDF";
  this.family.OTHER = "OTHER";

  this.eventNewFile = "vdui_event_newComplexObs";

  this.getProvider = function() {
    return "visitdocumentsui";
  }

  this.getPath = function(openmrsContextPath) {
    return openmrsContextPath + '/' + this.getProvider();
  }

  /**
  * Turns a byte array into a Base64 encoded String.
  * See http://stackoverflow.com/a/9458996/321797
  */
  this.arrayBufferToBase64 = function(buffer) {
    var binary = '';
    var bytes = new Uint8Array(buffer);
    var len = bytes.byteLength;
    for (var i = 0; i < len; i++) {
      binary += String.fromCharCode(bytes[i]);
    }
    return window.btoa(binary);
  }
}