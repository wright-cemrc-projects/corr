package org.cemrc.autodoc;

/**
 * This enum describes
 * @author larso
 *
 * See documentation at https://bio3d.colorado.edu/SerialEM/betaHlp/html/about_formats.htm
 */
public enum NavigatorKey {
	// Possible values for the NavigatorKey
	Color ("Color", NavigatorType.Integer), 
	StageXYZ ("StageXYZ", NavigatorType.TripleFloat), 
	NumPts ("NumPts", NavigatorType.Integer), 
	Corner ("Corner", NavigatorType.Integer), 
	Draw ("Draw", NavigatorType.Integer), 
	RegPt ("RegPt", NavigatorType.Integer), 
	Regis ("Regis", NavigatorType.Integer), 
	Type ("Type", NavigatorType.Integer), 
	Note ("Note", NavigatorType.String), 
	GroupID ("GroupID", NavigatorType.Integer),
	PolyID ("PolyID", NavigatorType.Integer),
	Imported("Imported", NavigatorType.Integer),
	RegisteredToID("RegisteredToID", NavigatorType.Integer),
	SuperMontXY("SuperMontXY", NavigatorType.TwoInteger),
	OrigReg("OrigReg", NavigatorType.Integer),
	DrawnID("DrawnID", NavigatorType.Integer),
	BklshXY("BklshXY", NavigatorType.TwoFloat),
	SamePosId("SamePosId", NavigatorType.Integer),
	RawStageXY("RawStageXY", NavigatorType.TwoFloat),
	Acquire("Acquire", NavigatorType.Integer),
	PieceOn("PieceOn", NavigatorType.Integer),
	XYinPc("XYinPc", NavigatorType.TwoFloat),
	MapFile("MapFile", NavigatorType.String),
	MapID("MapID", NavigatorType.Integer),
	FocusAxisPos("FocusAxisPos", NavigatorType.Float),
	LDAxisAngle("LDAxisAngle", NavigatorType.TwoFloat),
	FocusOffsets("FocusOffsets", NavigatorType.TwoFloat),
	HoleArray("HoleArray", NavigatorType.TwoInteger),
	SkipHoles("SkipHoles", NavigatorType.FloatList),
	MapMontage("MapMontage", NavigatorType.Integer),
	MapSection("MapSection", NavigatorType.Integer),
	MapBinning("MapBinning", NavigatorType.Integer),
	MapMagInd("MapMagInd", NavigatorType.Integer),
	MapCamera("MapCamera", NavigatorType.Integer),
	MapScaleMat("MapScaleMat", NavigatorType.FourFloat),
	MapWidthHeight("MapWidthHeight", NavigatorType.TwoInteger),
	MapMinMaxScale("MapMinMaxScale", NavigatorType.TwoFloat),
	MapFramesXY("MapFramesXY", NavigatorType.TwoInteger),
	MontBinning("MontBinning", NavigatorType.Integer),
	MapExposure("MapExposure", NavigatorType.Float),
	MapSettling("MapSettling", NavigatorType.Float),
	ShutterMode("ShutterMode", NavigatorType.Integer),
	MapSpotSize("MapSpotSize", NavigatorType.Integer),
	MapIntensity("MapIntensity", NavigatorType.Double),
	MapSlitIn("MapSlitIn", NavigatorType.Integer),
	MapSlitWidth("MapSlitWidth", NavigatorType.Integer),
	RotOnLoad("RotOnLoad", NavigatorType.Integer),
	RealignedID("RealignedID", NavigatorType.Integer),
	RealignErrXY("RealignErrXY", NavigatorType.TwoFloat),
	LocalErrXY("LocalErrXY", NavigatorType.TwoFloat),
	RealignReg("RealignReg", NavigatorType.Integer),
	ImageType("ImageType", NavigatorType.Integer),
	MontUseStage("MontUseStage", NavigatorType.Integer),
	DefocusOffset("DefocusOffset", NavigatorType.Float),
	K2ReadMode("K2ReadMode", NavigatorType.Integer),
	NetViewShiftXY("NetViewShiftXY", NavigatorType.TwoFloat),
	MapAlpha("MapAlpha", NavigatorType.Integer),
	ViewBeamShiftXY("ViewBeamShiftXY", NavigatorType.TwoFloat),
	ViewBeamTiltXY("ViewBeamTiltXY", NavigatorType.TwoFloat),
	MapProbeMode("MapProbeMode", NavigatorType.Integer),
	MapLDConSet("MapLDConSet", NavigatorType.Integer),
	MapTiltAngle("MapTiltAngle", NavigatorType.Float),
	PtsX("PtsX", NavigatorType.FloatList),
	PtsY("PtsY", NavigatorType.FloatList),
	Unknown ("Unknown", NavigatorType.String);
	
	private String text;
	private NavigatorType type;
	
	NavigatorKey(String text, NavigatorType type) {
		this.text = text;
		this.type = type;
	}
	
	public static NavigatorKey fromString(String text) {
        for (NavigatorKey b : NavigatorKey.values()) {
            if (b.text.equalsIgnoreCase(text)) {
                return b;
            }
        }
        System.out.println("Unable to get key for: " + text);
        return Unknown;
	}
	
	public NavigatorType getType() {
		return type;
	}
}
