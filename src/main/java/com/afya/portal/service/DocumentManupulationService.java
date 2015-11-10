package com.afya.portal.service;

import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xwpf.usermodel.*;
import org.apache.poi.xwpf.converter.pdf.PdfOptions;
import org.apache.poi.xwpf.converter.pdf.PdfConverter;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Raghu Bandi on 16-Oct-2015.
 */

@Service
public class DocumentManupulationService {

    public DocumentManupulationService(){

    }

    //The following method takes an INPUT word document and replaces required placeholders and OUTPUTs
    // a new word document

    public String returnPdfFilePath(String path, Map<String, Object> mailModel) {

        Map<String, String> documentMap = new HashMap<String, String>();

        if(mailModel.get("facility") != null) {
            documentMap.put("Community Member", mailModel.get("facility").toString());
            documentMap.put("Member", mailModel.get("facility").toString());
        } else {
            documentMap.put("Community Member", mailModel.get("firstName").toString()+" "+mailModel.get("lastName").toString());
        }

        return replacePlaceHoldersInTheFile(path,documentMap);
    }

    private String replacePlaceHoldersInTheFile (String path, Map<String, String> documentMap){

        try {
            InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
            XWPFDocument doc = new XWPFDocument(OPCPackage.open(inputStream));

            Set<String> set = documentMap.keySet();

            for (XWPFParagraph p : doc.getParagraphs()) {
                List<XWPFRun> runs = p.getRuns();
                if (runs != null) {
                    for (XWPFRun r : runs) {
                        String text = r.getText(0);
                        //System.out.println("Text value is :" + text);
                        for(String search : set) {
                            if (text != null && text.contains(search)) {
                                String newText = text.replace(search, documentMap.get(search));
                                r.setText(newText, 0);
                            }
                            }

                    }
                }
            }

            for (XWPFTable tbl : doc.getTables()) {
                for (XWPFTableRow row : tbl.getRows()) {
                    for (XWPFTableCell cell : row.getTableCells()) {
                        for (XWPFParagraph p : cell.getParagraphs()) {
                            for (XWPFRun r : p.getRuns()) {
                                String text = r.getText(0);
                                //System.out.println("Text value is :" + text);
                                for(String search : set) {
                                    if (text != null && text.contains(search)) {
                                        String newText = text.replace(search, documentMap.get(search));
                                        r.setText(newText, 0);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            File modifiedFile = new File("ModifiedDoc.docx");
            FileOutputStream fos = new FileOutputStream(modifiedFile);
            doc.write(fos);
            /*byte[] buffer = new byte[1024];
            int len = inputStream.read(buffer);
            while (len != -1) {
                fos.write(buffer, 0, len);
                len = inputStream.read(buffer);
            }*/
            return convertWordToPdf(modifiedFile.getAbsolutePath());
            //return modifiedFile.getAbsolutePath();
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return "Failed to convert into pdf document1";
    }

    //The following method converts word document to a pdf document

    private String convertWordToPdf(String fileName) {

        XWPFDocument document = null;
        try {

            // 1) Load DOCX into XWPFDocument
            InputStream in= new FileInputStream(new File(fileName));
            document = new XWPFDocument(in);

            // 2) Prepare Pdf options
            PdfOptions options = PdfOptions.create();
            options = PdfOptions.create().fontEncoding("windows-1250");

            // 3) Convert XWPFDocument to Pdf

            File pdfFile = new File("ModifiedPdf.pdf");
            OutputStream out = new FileOutputStream(pdfFile);
            PdfConverter.getInstance().convert(document, out, options);
            return pdfFile.getAbsolutePath();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return "Failed to convert into pdf document2";
    }
}
