import ceylon.language.meta { modules }
import java.util { JList = ArrayList }
import java.lang { JString = String }

shared JList<JString> lister() {
	value ret = JList<JString>();
	for (mod in modules.list) {
		JString js = JString(mod.name.string);
		ret.add(js);
	}
	return ret;
}