var module = new function() {

	var artifactId = "visitdocumentsui";

	this.getProvider = function() {
		return artifactId;
	}

	this.getPath = function(openmrsContextPath) {
		return openmrsContextPath + '/' + this.getProvider();
	}
}