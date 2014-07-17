package de.unifrankfurt.faststring.transform.patches;

import com.ibm.wala.shrikeBT.IInvokeInstruction.Dispatch;
import com.ibm.wala.shrikeBT.InvokeInstruction;
import com.ibm.wala.shrikeBT.LoadInstruction;
import com.ibm.wala.shrikeBT.MethodEditor;
import com.ibm.wala.shrikeBT.MethodEditor.Output;
import com.ibm.wala.shrikeBT.MethodEditor.Patch;
import com.ibm.wala.shrikeBT.StoreInstruction;
import com.ibm.wala.shrikeBT.Util;

import de.unifrankfurt.faststring.analysis.graph.MethodCallNode;
import de.unifrankfurt.faststring.analysis.label.TypeLabel;
import de.unifrankfurt.faststring.transform.TransformationInfo;


public class ConversationPatchFactory {

	private MethodEditor editor;
	private TransformationInfo info;
	private TypeLabel from;
	private TypeLabel to;


	public ConversationPatchFactory(TransformationInfo transformationInfo, MethodEditor editor, TypeLabel from, TypeLabel to) {
		this.info = transformationInfo;
		this.editor = editor;
		this.from = from;
		this.to = to;
		
	}


	public ConversationPatchFactory(TransformationInfo transformationInfo, MethodEditor editor, TypeLabel label) {
		this(transformationInfo, editor, null, label);
	}


	public void createConversationAtStart(int local) {
		editor.insertAtStart(new LoadFromLocalConversationPatch(from, to, local, info.getLocalForLabel(from, to, local)));
	}

	public void createConversationAfter(int local, int bcIndex) {
		Patch patch = createPatch(local);

		editor.insertAfter(bcIndex, patch);
	}


	private ConversationToLabelPatch createPatch(int local) {
		return (local != -1) ?
				new OnStackConversationPatch(from, to, info.getLocalForLabel(from, to, local)) :
				new ConversationToLabelPatch(from, to);
	}

	public void createConversationBefore(int bcIndex) {
		createConversationBefore(-1, bcIndex);
	}


	public void createConversationAfter(int bcIndex) {
		createConversationAfter(-1, bcIndex);
	}


	public void createConversationBefore(int local, int byteCodeIndex) {
		editor.insertBefore(byteCodeIndex, createPatch(local));
	}


	public void replaceLoad(int local, Integer loadIndex, TypeLabel label) {
		final String type = Util.makeType(label.getOptimizedType());
		
		if (loadIndex != null) {			
			final int optLocal = info.getLocalForLabel(null, label, local);
			
			editor.replaceWith(loadIndex, new Patch() {
				@Override
				public void emitTo(Output w) {
					w.emit(LoadInstruction.make(type, optLocal));
					
				}
			});
		}
	}


	public void replaceStore(int local, Integer storeIndex, TypeLabel label) {
		final String type = Util.makeType(label.getOptimizedType());
		
		if (storeIndex != null) {
			final int optLocal = info.getLocalForLabel(from, to, local);
	
			editor.replaceWith(storeIndex, new Patch() {
				@Override
				public void emitTo(Output w) {
					w.emit(StoreInstruction.make(type, optLocal));
					
				}
			});
		}
	}


	public void replaceMethodCall(MethodCallNode node) {
		int bcIndex = node.getByteCodeIndex();

		final String type = Util.makeType(node.getLabel().getOptimizedType());
		
		final InvokeInstruction invoke = (InvokeInstruction) editor.getInstructions()[bcIndex];
		
		final InvokeInstruction invokeOpt = InvokeInstruction.make(invoke.getMethodSignature(),
				type, invoke.getMethodName(), Dispatch.VIRTUAL);

		editor.replaceWith(node.getByteCodeIndex(), new Patch() {
			@Override
			public void emitTo(Output w) {
				w.emit(invokeOpt);
			}
		});
		
	}

}
