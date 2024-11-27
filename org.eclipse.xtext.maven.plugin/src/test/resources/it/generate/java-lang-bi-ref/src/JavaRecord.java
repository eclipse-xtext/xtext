public record JavaRecord(String name) {
	public void useXbaseType(XbaseReferToJava xbaseType) throws Throwable {
		xbaseType.myMethod();
	}
}
