package de.unifrankfurt.faststring.transform.patches;

import com.ibm.wala.shrikeBT.MethodEditor;
import com.ibm.wala.shrikeBT.MethodEditor.Patch;

import de.unifrankfurt.faststring.transform.TransformationInfo;
import de.unifrankfurt.faststring.transform.TransformationInfo.Constant;


public class PatchFactory {

	private MethodEditor editor;

	private TransformationInfo info;


	public PatchFactory(TransformationInfo transformationInfo, MethodEditor editor) {
		this.info = transformationInfo;
		this.editor = editor;
	}


	public void createConstantDefinition(final Constant constant) {
		insertAtStart(new ConstantDefinitionConversionPatch(info.getLabel(), constant));
	}

	public void createOptConversation(int local) {
		insertAtStart(new OptConversationPatch(info.getLabel(), local, info.getOptLocal(local)));
	}

	private void insertAtStart(Patch patch) {
		editor.insertAtStart(patch);
	}

	public void createOptConversationFromStack(int local, int bcIndex) {
		editor.insertAfter(bcIndex, new OnStackOptConversationPatch(info.getLabel(), info.getOptLocal(local)));
	}

}
