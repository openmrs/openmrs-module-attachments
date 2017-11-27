package org.openmrs.module.attachments.obs;

import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.openmrs.obs.ComplexData;
import org.openmrs.web.servlet.ComplexObsServlet;

/**
 * Extends {@link ComplexData} so that it provides a method to extract the underlying byte array.
 * Whenever we need to extend {@link ComplexData} we should actually extend this class.
 * 
 * @author Mekom Solutions
 */
public class BaseComplexData extends ComplexData {
	
	private static final long serialVersionUID = 1L;
	
	public BaseComplexData(String title, Object data) {
		super(title, data);
	}
	
	/**
	 * This returns the byte array out of a complex data's inner data. This is borrowed from
	 * {@link ComplexObsServlet}.
	 * 
	 * @return The byte array, or an empty array if an error occurred.
	 */
	public static byte[] getByteArray(ComplexData complexData) {
		final byte[] emptyContent = new byte[0];
		
		Object data = (complexData != null) ? complexData.getData() : emptyContent;
		
		if (data == null) {
			return emptyContent;
		}
		if (data instanceof byte[]) {
			return (byte[]) data;
		} else if (RenderedImage.class.isAssignableFrom(data.getClass())) {
			RenderedImage image = (RenderedImage) data;
			
			ByteArrayOutputStream bytesOutStream = new ByteArrayOutputStream();
			try {
				ImageOutputStream imgOutStream = ImageIO.createImageOutputStream(bytesOutStream);
				String extension = FilenameUtils.getExtension(complexData.getTitle());
				ImageIO.write(image, extension, imgOutStream);
				imgOutStream.close();
			}
			catch (IOException e) {
				return emptyContent;
			}
			
			return bytesOutStream.toByteArray();
		} else if (InputStream.class.isAssignableFrom(data.getClass())) {
			try {
				return IOUtils.toByteArray((InputStream) data);
			}
			catch (IOException e) {
				return emptyContent;
			}
		} else {
			return emptyContent;
		}
	}
	
	public byte[] asByteArray() {
		return getByteArray(this);
	}
}
