

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.pdfparser.PDFStreamParser;
import org.apache.pdfbox.pdfwriter.ContentStreamWriter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.commons.lang.StringUtils;
//import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.contentstream.PDFStreamEngine;


import java.io.OutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main extends PDFStreamEngine
{

	public static PDPage page1;
	public static PDDocument document = null;
	public static PDFont font;
	public static void main( String[] args ) throws IOException
	{
		String fileName = "PDFs/Impositioned.pdf";
		try
		{
			document = PDDocument.load(new File(fileName));
			System.out.println( "Pages: " + document.getNumberOfPages());

			PDDocument newdocument = PDDocument.load(new File(fileName));

			for (int i = 0; i < document.getNumberOfPages(); i++) {
				
				page1 = (PDPage) newdocument.getDocumentCatalog().getPages().get(i); 
				
				
				removeAllTextTokens(page1, document); 

				GetCharLocationAndSize.main2(page1);

			}


			newdocument = new PDDocument();

			for (int i = 0; i < document.getNumberOfPages(); i++) {		
				newdocument.importPage(page1);
			}


			newdocument.save( "Hello World.pdf");
			newdocument.close();


		}
		finally
		{
			if( document != null )
			{
				document.close();
			}
		}
	}

	public static PDDocument replaceText(PDDocument document, String searchString, String replacement) throws IOException {
	      final PDPage page = document.getPage(1);
	      PDFStreamParser parser = new PDFStreamParser(page);
	      parser.parse();
	      List<Object> tokens =parser.getTokens();
	      
	      for (int j = 0; j < tokens.size(); j++) {
	        Object next = ((java.util.List<Object>) tokens).get(j);
	        if (next instanceof Operator) {
	          Operator op = (Operator) next;
	          //Tj and TJ are the two operators that display strings in a PDF
	          if (op.getName().equals("Tj")) {
	            // Tj takes one operator and that is the string to display so lets update that operator
	            COSString previous = (COSString) tokens.get(j - 1);
	            String string = previous.getString();
	            string = string.replaceFirst(searchString, replacement);
	            previous.setValue(string.getBytes());
	            System.out.println(previous.getString());
	          } else if (op.getName().equals("TJ")) {
	            COSArray previous = (COSArray) ((java.util.List<Object>) tokens).get(j - 1);
	            for (int k = 0; k < previous.size(); k++) {
	              Object arrElement = previous.getObject(k);
	              if (arrElement instanceof COSString) {
	                COSString cosString = (COSString) arrElement;
	                String string = cosString.getString();
	                string = StringUtils.replaceOnce(string, searchString, replacement);
	                cosString.setValue(string.getBytes());
	              }
	            }
	          }
	        }
	      }
	      // now that the tokens are updated we will replace the page content stream.
	      PDStream updatedStream = new PDStream(document);
	      OutputStream out = (OutputStream) updatedStream.createOutputStream();
	      ContentStreamWriter tokenWriter = new ContentStreamWriter(out);
	      tokenWriter.writeTokens(tokens);
	      page.setContents(updatedStream);
	      out.close();
	      return document;
	    }


	

	private static void removeAllTextTokens(PDPage page, PDDocument document) throws IOException
	{
		PDFStreamParser parser = new PDFStreamParser(page);
		parser.parse();
		List<Object> tokens = parser.getTokens();
		List<Object> newTokens = new ArrayList<>();
		for (Object token : tokens)
		{
			if (token instanceof Operator)
			{
				String opname = ((Operator) token).getName();
				if ("TJ".equals(opname) || "Tj".equals(opname))
				{
					// remove the one argument to this operator
					newTokens.remove(newTokens.size()-1);
					continue;
				}
			}
			newTokens.add(token);
		}
		PDStream newContents = new PDStream(document);
		try (OutputStream out = newContents.createOutputStream(COSName.FLATE_DECODE))
		{
			ContentStreamWriter writer = new ContentStreamWriter(out);
			writer.writeTokens(newTokens);
		}
		page.setContents(newContents);
		processResources(page.getResources());
	}

	private static void processResources(PDResources resources) throws IOException
	{
		Iterable<COSName> names = resources.getXObjectNames();
		for (COSName name : names)
		{
			PDXObject xobject = resources.getXObject(name);
			if (xobject instanceof PDFormXObject)
			{
				removeAllTextTokens((PDFormXObject) xobject);
			}
		}
	}

	private static void removeAllTextTokens(PDFormXObject xobject) throws IOException{

		PDStream stream = xobject.getContentStream();
		PDFStreamParser parser = new PDFStreamParser(xobject);
		parser.parse();
		List<Object> tokens = parser.getTokens();
		List<Object> newTokens = new ArrayList<>();
		for (Object token : tokens)
		{
			if (token instanceof Operator)
			{
				Operator op = (Operator) token;
				if ("TJ".equals(op.getName()) || "Tj".equals(op.getName()) ||
						"'".equals(op.getName()) || "\"".equals(op.getName()))
				{
					// remove the one argument to this operator
					newTokens.remove(newTokens.size() - 1);
					continue;
				}
			}
			newTokens.add(token);
		}
		try (OutputStream out = stream.createOutputStream(COSName.FLATE_DECODE))
		{
			ContentStreamWriter writer = new ContentStreamWriter(out);
			writer.writeTokens(newTokens);
		}
		processResources(xobject.getResources());
	}

}