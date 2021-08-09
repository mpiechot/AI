package ai;

import ai.implementation.MyCodeBattleClient;
import de.itdesign.codebattle.client.CodeBattleEntryPoint;

public class EntryPoint extends CodeBattleEntryPoint {

    public static void main(String[] args) {
        init(MyCodeBattleClient.class);
    }
}
