import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import org.apache.pdfbox.util.Matrix;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

/**
 * This is an example on how to get the x/y coordinates and size of each character in PDF
 */
public class GetCharLocationAndSize extends PDFTextStripper {

	public GetCharLocationAndSize() throws IOException {
	}
	public int pagenum = 0;
	public int counter = 0;
	public static PDPage page;
	/**
	 * @throws IOException If there is an error parsing the document.
	 */
	public static void main2(PDPage p) throws IOException	{
		page = p;
			PDFTextStripper stripper = new GetCharLocationAndSize();
            stripper.setSortByPosition( true );
            stripper.setStartPage( 0 );
            stripper.setEndPage( Main.document.getNumberOfPages() );

            Writer dummy = new OutputStreamWriter(new ByteArrayOutputStream());
            stripper.writeText(Main.document, dummy);

	}

	/**
	 * Override the default functionality of PDFTextStripper.writeString()
	 */
	@Override
	protected void writeString(String string, List<TextPosition> textPositions) throws IOException {
		
		System.out.println("text size " + textPositions.size());
			for (TextPosition text : textPositions) {
				System.out.println(text.getUnicode()+ " [(X=" + text.getXDirAdj() + ",Y=" +
						text.getYDirAdj() + ") height=" + text.getHeightDir() + " width=" +
						text.getWidthDirAdj() + "]");
				
				PDPageContentStream contents = new PDPageContentStream(Main.document, page, true, true);
				contents.beginText();
	            PDRectangle pageSize = page.getMediaBox();


				contents.newLineAtOffset(text.getX(), text.getY() + 500);

				contents.setFont(text.getFont(), text.getFontSize());
				contents.showText(text.getUnicode());

				contents.endText();

				contents.close();
				



			}
	}
	
	public static ArrayList<Float> getXDirAdj(String string, List<TextPosition> textPositions) {
		ArrayList<Float> x = new ArrayList<>();
		for (TextPosition text : textPositions) {
			x.add(text.getXDirAdj());
		}
		return x;
	}
	
	public static ArrayList<Float> getYDirAdj(String string, List<TextPosition> textPositions) {
		ArrayList<Float> y = new ArrayList<>();
		for (TextPosition text : textPositions) {
			y.add(text.getYDirAdj());
		}
		return y;
	}
	
	public static ArrayList<Float> getHeightDirAdj(String string, List<TextPosition> textPositions) {
		ArrayList<Float> height = new ArrayList<>();
		for (TextPosition text : textPositions) {
			height.add(text.getHeightDir());
		}
		return height;
	}
	
	public static ArrayList<Float> getWidthDirAdj(String string, List<TextPosition> textPositions) {
		ArrayList<Float> width = new ArrayList<>();
		for (TextPosition text : textPositions) {
			width.add(text.getWidthDirAdj());
		}
		return width;
	}

}