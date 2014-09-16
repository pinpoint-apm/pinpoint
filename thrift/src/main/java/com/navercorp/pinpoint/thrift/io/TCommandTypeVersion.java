package com.nhn.pinpoint.thrift.io;

import java.util.ArrayList;
import java.util.List;

import org.apache.thrift.TBase;

/**
 * @author koo.taejin
 */
public enum TCommandTypeVersion {

	// Agent 버젼과 맞추면 좋을듯 일단은 Agent 버전과 맞춰놓음
	V_1_0_2_SNAPSHOT("1.0.2-SNAPSHOT", TCommandType.RESULT, TCommandType.THREAD_DUMP),
	V_1_0_2("1.0.2", V_1_0_2_SNAPSHOT),
	V_1_0_3_SNAPSHOT("1.0.3-SNAPSHOT", V_1_0_2, TCommandType.ECHO, TCommandType.TRANSFER),
	
	UNKNOWN("UNKNOWN");

	private final String versionName;
	private final List<TCommandType> supportCommandList = new ArrayList<TCommandType>();

	private TCommandTypeVersion(String versionName, TCommandTypeVersion version, TCommandType... supportCommandArray) {
		this.versionName = versionName;
		
		for (TCommandType supportCommand : version.getSupportCommandList()) {
			supportCommandList.add(supportCommand);
		}

		for (TCommandType supportCommand : supportCommandArray) {
			getSupportCommandList().add(supportCommand);
		}
	}

	private TCommandTypeVersion(String versionName, TCommandType... supportCommandArray) {
		this.versionName = versionName;

		for (TCommandType supportCommand : supportCommandArray) {
			getSupportCommandList().add(supportCommand);
		}
	}

	public List<TCommandType> getSupportCommandList() {
		return supportCommandList;
	}
	
	public boolean isSupportCommand(TBase command) {
		if (command == null) {
			return false;
		}
		
		for (TCommandType eachCommand : supportCommandList) {
			if (command.getClass().isInstance(eachCommand)) {
				return true;
			}
		}

		return false;
	}

	public String getVersionName() {
		return versionName;
	}
	
	public static TCommandTypeVersion getVersion(String version) {
		if (version == null) {
			throw new NullPointerException("version may not be null.");
		}
		
		for (TCommandTypeVersion versionType : TCommandTypeVersion.values()) {
			if (versionType.getVersionName().equals(version)) {
				return versionType;
			}
		}
		
		return UNKNOWN;
	}

}
