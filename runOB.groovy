import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.expressions.ExpressionHelper;
import com.nomagic.magicdraw.expressions.ParameterizedExpression;
import com.nomagic.magicdraw.ui.browser.Browser;
import com.nomagic.uml2.ext.magicdraw.commonbehaviors.mdbasicbehaviors.Behavior;

		Browser browser = Application.getInstance().getMainFrame().getBrowser();
		Object obj = browser.getActiveTree().getSelectedNode().getUserObject();
		
		if(obj == null || !(obj instanceof Behavior)) { 
			return;
		}

		Behavior element = (Behavior)obj;
		
		ParameterizedExpression behaviorExpression = ExpressionHelper.getBehaviorExpression((Behavior)element);
		if(behaviorExpression != null) {
			try {
				ExpressionHelper.call(behaviorExpression);
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}