package test

class XtendA extends JavaB {

	def JavaB test2(XtendC s) {
		return s.foo.newJavaB
	}

	def JavaB newJavaB() {
		return new JavaB()
	}

	def JavaRecord newJavaRecord() {
		return new JavaRecord("a test")
	}
}