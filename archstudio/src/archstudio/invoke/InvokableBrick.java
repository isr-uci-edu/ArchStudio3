package archstudio.invoke;

/**
 * Describes a <CODE>Brick</CODE> that can be invoked by the ArchStudio File Manager/Invoker
 * component.
 * @author Eric M. Dashofy <A HREF="mailto:edashofy@ics.uci.edu">edashofy@ics.uci.edu</A>
 */
public interface InvokableBrick extends c2.fw.Brick{
	
	/**
	 * This function is called automatically when the <CODE>Invokable</CODE> object
	 * should be invoked.
	 * @param im <CODE>InvokeMessage</CODE> containing the request for invocation.
	 */
	public void invoke(InvokeMessage im);

}
