package Util;

/**
 * enum to represent all situations on the gamefield.
 * alphabeta determines the winner on the celltypes -INWALL and -INSNAKE 
 */
public enum Type 
{
	WALL,
	SPACE,
	MYSNAKE,
	ENEMYSNAKE,
	APPLE,
	ENEMYINWALL,
	MYSNAKEINWALL,
	MYSNAKEINSNAKE,
	ENEMYSNAKEINSNAKE,
	WALLFEATURE,
	CHANGEHEADTAIL,
	CHANGESNAKE,
	SPEEDUP,
	PORTAL,
	CUTTAIL,
	OPENFIELD,
	OPENFIELDPICTURE
}
