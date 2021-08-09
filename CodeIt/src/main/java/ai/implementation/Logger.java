package ai.implementation;

import de.itdesign.codebattle.api.codeinterface.CodeBattleClientImpl;

public final class Logger {
	public static CodeBattleClientImpl ki;
	
	public Logger(CodeBattleClientImpl ki)
	{
		if(this.ki == null)
			this.ki = ki;
	}
	public void log(String message)
	{
		ki.log(message);
	}
}
