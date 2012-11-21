package edu.mayo.pipes.bioinformatics;

import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


import com.tinkerpop.pipes.AbstractPipe;
import edu.mayo.pipes.records.Variant;
import edu.mayo.pipes.util.GenomicObjectUtils;
import edu.mayo.pipes.util.JSONUtil;

import java.util.NoSuchElementException;

public class VCF2VariantPipe extends AbstractPipe<String,Variant>{
    
    ArrayList<String> header = new ArrayList<String>();
    public VCF2VariantPipe(){
        
    }
    private int count =0;
    
    public void initializeHeader(){
        
    }
    
    public Variant compute(String l) {
        String line = l;
        
        while(line.startsWith("#")){
            header.add(line);
            line = this.starts.next();
        }
        
        String s[] = line.split(line);
        if(s.length < 8){
            throw new NoSuchElementException();
        }
        
        JsonObject payload = new JsonObject();
        
        //0CHROM	1POS	2ID 3REF	4ALT	5QUAL	6FILTER	7INFO
        Variant variant = new Variant();
        
        variant.setChr(GenomicObjectUtils.computechr(s[0]));
        payload.addProperty("CHROM", s[0]);
        
        ++count;
//        if(!s[2].startsWith("rs")){
//            variant.setId(count+"_"+s[1]+"_"+s[0]);
//        }
        variant.setId(s[2]);//guarenteed to be unique, if no then perhaps bug
        payload.addProperty("ID", s[2]);
        
        variant.setRefAllele(s[3]); 
        payload.addProperty("REF", s[3]);
        
        String[] al = al(s[4]);
        variant.setAltAllele(al); 
        payload.add("ALT", JSONUtil.stringArr2JSON(al));
        
        if (s[1]!=null) {
            int start = new Integer(s[1]);        
            variant.setMinBP(start);
            payload.addProperty("POS", start);
            variant.setMaxBP(new Integer(start + s[3].length()-1));        	
        } else {
            variant.setMinBP(0);
            variant.setMaxBP(0);
            payload.addProperty("POS", 0);
        }
        //variant.setQual(s[5]);
        payload.addProperty("QUAL", s[5]);
        //variant.setFilter(s[6]);
        payload.addProperty("FILTER", s[6]);
        //variant.setOneLiner(s[7]);
        HashMap hash = populate(s[7].split(";"));
        JsonObject info = new JsonObject();
        
        variant.setType(getTypeFromVCF(variant));
        payload.addProperty("variant_type", getTypeFromVCF(variant));
        //payload.addProperty("type", "Variant");
        
        //variant.setProperties(hash);
        payload.add("INFO", info);
        
        //System.out.println(variant.toString());
        
        return variant;
    }
    
    
    
    private String getTypeFromVCF(Variant v){
        String[] altAllele = v.getAltAllele();
        if (v.getRefAllele().length() == altAllele[0].length()){
            return "SNP";
        }
        if (v.getRefAllele().length() < altAllele[0].length()){
            return "insertion";
        }
        if (v.getRefAllele().length() > altAllele[0].length()){
            return "deletion";
        }
        else return "unknown";
    }

    private String[] al(String raw){
        ArrayList finalList = new ArrayList();
        if(raw.contains(",")){
            //System.out.println(raw);
            String[] split = raw.split(",");
            for(int i = 0; i<split.length; i++){
                finalList.add(split[i]);
            }
        }else{
            finalList.add(raw);
        }
        return (String[]) finalList.toArray( new String[0] ); //finalList.size()
    }
    
    private HashMap populate(String[] data){
        HashMap hm = new HashMap();
        for(int i = 0; i<data.length; i++){
            //System.out.println(data[i]);
            if(data[i].contains("=")){
                String[] tokens = data[i].split("=");
                hm.put(tokens[0], tokens[1]);
            }else {
                hm.put(data[i], "1");
            }
        }
        return hm;
    }

    @Override
    protected Variant processNextStart() throws NoSuchElementException {
        String s = this.starts.next();

        return compute(s);
    }
}
/*
##fileformat=VCFv4.0
##fileDate=20120616
##source=dbSNP
##dbSNP_BUILD_ID=137
##reference=GRCh37.p5
##phasing=partial
##variationPropertyDocumentationUrl=ftp://ftp.ncbi.nlm.nih.gov/snp/specs/dbSNP_BitField_latest.pdf	
##INFO=<ID=RSPOS,Number=1,Type=Integer,Description="Chr position reported in dbSNP">
##INFO=<ID=RV,Number=0,Type=Flag,Description="RS orientation is reversed">
##INFO=<ID=VP,Number=1,Type=String,Description="Variation Property.  Documentation is at ftp://ftp.ncbi.nlm.nih.gov/snp/specs/dbSNP_BitField_latest.pdf">
##INFO=<ID=GENEINFO,Number=1,Type=String,Description="Pairs each of gene symbol:gene id.  The gene symbol and id are delimited by a colon (:) and each pair is delimited by a vertical bar (|)">
##INFO=<ID=dbSNPBuildID,Number=1,Type=Integer,Description="First dbSNP Build for RS">
##INFO=<ID=SAO,Number=1,Type=Integer,Description="Variant Allele Origin: 0 - unspecified, 1 - Germline, 2 - Somatic, 3 - Both">
##INFO=<ID=SSR,Number=1,Type=Integer,Description="Variant Suspect Reason Code, 0 - unspecified, 1 - Paralog, 2 - byEST, 3 - Para_EST, 4 - oldAlign, 5 - other">
##INFO=<ID=GMAF,Number=1,Type=Float,Description="Global Minor Allele Frequency [0, 0.5]; global population is 1000GenomesProject phase 1 genotype data from 629 individuals, released in the 11-23-2010 dataset">
##INFO=<ID=WGT,Number=1,Type=Integer,Description="Weight, 00 - unmapped, 1 - weight 1, 2 - weight 2, 3 - weight 3 or more">
##INFO=<ID=VC,Number=1,Type=String,Description="Variation Class">
##INFO=<ID=PM,Number=0,Type=Flag,Description="Variant is Precious(Clinical,Pubmed Cited)">
##INFO=<ID=TPA,Number=0,Type=Flag,Description="Provisional Third Party Annotation(TPA) (currently rs from PHARMGKB who will give phenotype data)">
##INFO=<ID=PMC,Number=0,Type=Flag,Description="Links exist to PubMed Central article">
##INFO=<ID=S3D,Number=0,Type=Flag,Description="Has 3D structure - SNP3D table">
##INFO=<ID=SLO,Number=0,Type=Flag,Description="Has SubmitterLinkOut - From SNP->SubSNP->Batch.link_out">
##INFO=<ID=NSF,Number=0,Type=Flag,Description="Has non-synonymous frameshift A coding region variation where one allele in the set changes all downstream amino acids. FxnClass = 44">
##INFO=<ID=NSM,Number=0,Type=Flag,Description="Has non-synonymous missense A coding region variation where one allele in the set changes protein peptide. FxnClass = 42">
##INFO=<ID=NSN,Number=0,Type=Flag,Description="Has non-synonymous nonsense A coding region variation where one allele in the set changes to STOP codon (TER). FxnClass = 41">
##INFO=<ID=REF,Number=0,Type=Flag,Description="Has reference A coding region variation where one allele in the set is identical to the reference sequence. FxnCode = 8">
##INFO=<ID=SYN,Number=0,Type=Flag,Description="Has synonymous A coding region variation where one allele in the set does not change the encoded amino acid. FxnCode = 3">
##INFO=<ID=U3,Number=0,Type=Flag,Description="In 3' UTR Location is in an untranslated region (UTR). FxnCode = 53">
##INFO=<ID=U5,Number=0,Type=Flag,Description="In 5' UTR Location is in an untranslated region (UTR). FxnCode = 55">
##INFO=<ID=ASS,Number=0,Type=Flag,Description="In acceptor splice site FxnCode = 73">
##INFO=<ID=DSS,Number=0,Type=Flag,Description="In donor splice-site FxnCode = 75">
##INFO=<ID=INT,Number=0,Type=Flag,Description="In Intron FxnCode = 6">
##INFO=<ID=R3,Number=0,Type=Flag,Description="In 3' gene region FxnCode = 13">
##INFO=<ID=R5,Number=0,Type=Flag,Description="In 5' gene region FxnCode = 15">
##INFO=<ID=OTH,Number=0,Type=Flag,Description="Has other variant with exactly the same set of mapped positions on NCBI refernce assembly.">
##INFO=<ID=CFL,Number=0,Type=Flag,Description="Has Assembly conflict. This is for weight 1 and 2 variant that maps to different chromosomes on different assemblies.">
##INFO=<ID=ASP,Number=0,Type=Flag,Description="Is Assembly specific. This is set if the variant only maps to one assembly">
##INFO=<ID=MUT,Number=0,Type=Flag,Description="Is mutation (journal citation, explicit fact): a low frequency variation that is cited in journal and other reputable sources">
##INFO=<ID=VLD,Number=0,Type=Flag,Description="Is Validated.  This bit is set if the variant has 2+ minor allele count based on frequency or genotype data.">
##INFO=<ID=G5A,Number=0,Type=Flag,Description=">5% minor allele frequency in each and all populations">
##INFO=<ID=G5,Number=0,Type=Flag,Description=">5% minor allele frequency in 1+ populations">
##INFO=<ID=HD,Number=0,Type=Flag,Description="Marker is on high density genotyping kit (50K density or greater).  The variant may have phenotype associations present in dbGaP.">
##INFO=<ID=GNO,Number=0,Type=Flag,Description="Genotypes available. The variant has individual genotype (in SubInd table).">
##INFO=<ID=KGValidated,Number=0,Type=Flag,Description="1000 Genome validated">
##INFO=<ID=KGPhase1,Number=0,Type=Flag,Description="1000 Genome phase 1 (incl. June Interim phase 1)">
##INFO=<ID=KGPilot123,Number=0,Type=Flag,Description="1000 Genome discovery all pilots 2010(1,2,3)">
##INFO=<ID=KGPROD,Number=0,Type=Flag,Description="Has 1000 Genome submission">
##INFO=<ID=OTHERKG,Number=0,Type=Flag,Description="non-1000 Genome submission">
##INFO=<ID=PH3,Number=0,Type=Flag,Description="HAP_MAP Phase 3 genotyped: filtered, non-redundant">
##INFO=<ID=CDA,Number=0,Type=Flag,Description="Variation is interrogated in a clinical diagnostic assay">
##INFO=<ID=LSD,Number=0,Type=Flag,Description="Submitted from a locus-specific database">
##INFO=<ID=MTP,Number=0,Type=Flag,Description="Microattribution/third-party annotation(TPA:GWAS,PAGE)">
##INFO=<ID=OM,Number=0,Type=Flag,Description="Has OMIM/OMIA">
##INFO=<ID=NOC,Number=0,Type=Flag,Description="Contig allele not present in variant allele list. The reference sequence allele at the mapped position is not present in the variant allele list, adjusted for orientation.">
##INFO=<ID=WTD,Number=0,Type=Flag,Description="Is Withdrawn by submitter If one member ss is withdrawn by submitter, then this bit is set.  If all member ss' are withdrawn, then the rs is deleted to SNPHistory">
##INFO=<ID=NOV,Number=0,Type=Flag,Description="Rs cluster has non-overlapping allele sets. True when rs set has more than 2 alleles from different submissions and these sets share no alleles in common.">
##INFO=<ID=GCF,Number=0,Type=Flag,Description="Has Genotype Conflict Same (rs, ind), different genotype.  N/N is not included.">
##FILTER=<ID=NC,Description="Inconsistent Genotype Submission For At Least One Sample">
#CHROM	POS	ID	REF	ALT	QUAL	FILTER	INFO
*/