package test

class XtendA {
	def JavaRecord newJavaRecord() {
		return new JavaRecord("a test")
	}

	def String aName(JavaRecord r) {
		r.name
	}
}