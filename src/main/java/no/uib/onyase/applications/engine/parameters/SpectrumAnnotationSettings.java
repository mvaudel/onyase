package no.uib.onyase.applications.engine.parameters;

import com.compomics.util.experiment.biology.ions.ReporterIon;

/**
 * Settings for the annotation of spectra.
 *
 * @author Marc Vaudel
 */
public class SpectrumAnnotationSettings {
    
    private boolean a = false;
    private boolean b = false;
    private boolean c = false;
    private boolean x = false;
    private boolean y = false;
    private boolean z = false;
    private boolean precursor = false;
    private boolean immonium = false;
    private boolean related = false;
    private boolean reporter = false;
    private boolean neutralLosses = false;
    private boolean neutralLossesSequenceDependent = false;
    private ReporterIon[] reporterIons;
    
    public SpectrumAnnotationSettings() {
        
    }

    public boolean isA() {
        return a;
    }

    public void setA(boolean a) {
        this.a = a;
    }

    public boolean isB() {
        return b;
    }

    public void setB(boolean b) {
        this.b = b;
    }

    public boolean isC() {
        return c;
    }

    public void setC(boolean c) {
        this.c = c;
    }

    public boolean isX() {
        return x;
    }

    public void setX(boolean x) {
        this.x = x;
    }

    public boolean isY() {
        return y;
    }

    public void setY(boolean y) {
        this.y = y;
    }

    public boolean isZ() {
        return z;
    }

    public void setZ(boolean z) {
        this.z = z;
    }

    public boolean isPrecursor() {
        return precursor;
    }

    public void setPrecursor(boolean precursor) {
        this.precursor = precursor;
    }

    public boolean isImmonium() {
        return immonium;
    }

    public void setImmonium(boolean immonium) {
        this.immonium = immonium;
    }

    public boolean isRelated() {
        return related;
    }

    public void setRelated(boolean related) {
        this.related = related;
    }

    public boolean isReporter() {
        return reporter;
    }

    public void setReporter(boolean reporter) {
        this.reporter = reporter;
    }

    public boolean isNeutralLosses() {
        return neutralLosses;
    }

    public void setNeutralLosses(boolean neutralLosses) {
        this.neutralLosses = neutralLosses;
    }

    public boolean isNeutralLossesSequenceDependent() {
        return neutralLossesSequenceDependent;
    }

    public void setNeutralLossesSequenceDependent(boolean neutralLossesSequenceDependent) {
        this.neutralLossesSequenceDependent = neutralLossesSequenceDependent;
    }

    public ReporterIon[] getReporterIons() {
        return reporterIons;
    }

    public void setReporterIons(ReporterIon[] reporterIons) {
        this.reporterIons = reporterIons;
    }
    
    

}
