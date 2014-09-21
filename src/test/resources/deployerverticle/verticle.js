var props = java.lang.System.getProperties();
props.put("lifecycle", "started");
function vertxStop() {
  props.put("lifecycle", "stopped");
}