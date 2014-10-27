package org.ripple.power.ui.graphics;

import java.awt.Color;
import java.awt.Image;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.awt.image.ImageProducer;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import org.ripple.power.config.LSystem;
import org.ripple.power.ui.UIRes;
import org.ripple.power.utils.GraphicsUtils;

public class LImage {

	public static class LFormatTGA {

		private static final int TGA_HEADER_SIZE = 18;

		private static final int TGA_HEADER_INVALID = 0;

		private static final int TGA_HEADER_UNCOMPRESSED = 1;

		private static final int TGA_HEADER_COMPRESSED = 2;

		public static class State {

			public int type;

			public int pixelDepth;

			public int width;

			public int height;

			public int[] pixels;

			public void dispose() {
				if (pixels != null) {
					pixels = null;
				}
			}

		}

		public static State inJustDecode(String res) throws IOException {
			return inJustDecode(UIRes.getStream(res));
		}

		public static State inJustDecode(InputStream in) throws IOException {
			return loadHeader(in, new State());
		}

		private static State loadHeader(InputStream in, State info)
				throws IOException {

			in.read();
			in.read();

			info.type = (byte) in.read();

			in.read();
			in.read();
			in.read();
			in.read();
			in.read();
			in.read();
			in.read();
			in.read();
			in.read();

			info.width = (in.read() & 0xff) | ((in.read() & 0xff) << 8);
			info.height = (in.read() & 0xff) | ((in.read() & 0xff) << 8);

			info.pixelDepth = in.read() & 0xff;

			return info;
		}

		private static final short getUnsignedByte(byte[] bytes, int byteIndex) {
			return (short) (bytes[byteIndex] & 0xFF);
		}

		private static final int getUnsignedShort(byte[] bytes, int byteIndex) {
			return (getUnsignedByte(bytes, byteIndex + 1) << 8)
					+ getUnsignedByte(bytes, byteIndex + 0);
		}

		private static void readBuffer(InputStream in, byte[] buffer)
				throws IOException {
			int bytesRead = 0;
			int bytesToRead = buffer.length;
			for (; bytesToRead > 0;) {
				int read = in.read(buffer, bytesRead, bytesToRead);
				bytesRead += read;
				bytesToRead -= read;
			}
		}

		private static final void skipBytes(InputStream in, long toSkip)
				throws IOException {
			for (; toSkip > 0L;) {
				long skipped = in.skip(toSkip);
				if (skipped > 0) {
					toSkip -= skipped;
				} else if (skipped < 0) {
					toSkip = 0;
				}
			}
		}

		private static final int compareFormatHeader(InputStream in,
				byte[] header) throws IOException {

			readBuffer(in, header);
			boolean hasPalette = false;
			int result = TGA_HEADER_INVALID;

			int imgIDSize = getUnsignedByte(header, 0);

			if ((header[1] != (byte) 0) && (header[1] != (byte) 1)) {
				return TGA_HEADER_INVALID;
			}

			switch (getUnsignedByte(header, 2)) {
			case 0:
				result = TGA_HEADER_UNCOMPRESSED;
				break;
			case 1:
				hasPalette = true;
				result = TGA_HEADER_UNCOMPRESSED;
				throw new RuntimeException(
						"Indexed State is not yet supported !");
			case 2:
				result = TGA_HEADER_UNCOMPRESSED;
				break;
			case 3:
				result = TGA_HEADER_UNCOMPRESSED;
				break;
			case 9:
				hasPalette = true;
				result = TGA_HEADER_COMPRESSED;
				throw new RuntimeException(
						"Indexed State is not yet supported !");
			case 10:
				result = TGA_HEADER_COMPRESSED;
				break;
			case 11:
				result = TGA_HEADER_COMPRESSED;
				break;
			default:
				return TGA_HEADER_INVALID;
			}
			if (!hasPalette) {
				if (getUnsignedShort(header, 3) != 0) {
					return TGA_HEADER_INVALID;
				}
			}
			if (!hasPalette) {
				if (getUnsignedShort(header, 5) != 0) {
					return TGA_HEADER_INVALID;
				}
			}

			short paletteEntrySize = getUnsignedByte(header, 7);
			if (!hasPalette) {
				if (paletteEntrySize != 0) {
					return TGA_HEADER_INVALID;
				}
			} else {
				if ((paletteEntrySize != 15) && (paletteEntrySize != 16)
						&& (paletteEntrySize != 24) && (paletteEntrySize != 32)) {
					return TGA_HEADER_INVALID;
				}
			}

			if (getUnsignedShort(header, 8) != 0) {
				return TGA_HEADER_INVALID;
			}

			if (getUnsignedShort(header, 10) != 0) {
				return TGA_HEADER_INVALID;
			}

			switch (getUnsignedByte(header, 16)) {
			case 1:
			case 8:
			case 15:
			case 16:
				throw new RuntimeException(
						"this State with non RGB or RGBA pixels are not yet supported.");
			case 24:
			case 32:
				break;
			default:
				return TGA_HEADER_INVALID;
			}

			if (imgIDSize != 0) {
				skipBytes(in, imgIDSize);
			}

			return result;
		}

		private static final void writePixel(int[] pixels, final byte red,
				final byte green, final byte blue, final byte alpha,
				final boolean hasAlpha, final int offset) {
			int pixel;
			if (hasAlpha) {
				pixel = (red & 0xff);
				pixel |= ((green & 0xff) << 8);
				pixel |= ((blue & 0xff) << 16);
				pixel |= ((alpha & 0xff) << 24);
				pixels[offset / 4] = pixel;
			} else {
				pixel = (red & 0xff);
				pixel |= ((green & 0xff) << 8);
				pixel |= ((blue & 0xff) << 16);
				pixels[offset / 4] = pixel;
			}
		}

		private static int[] readBuffer(InputStream in, int width, int height,
				int srcBytesPerPixel, boolean acceptAlpha,
				boolean flipVertically) throws IOException {

			int[] pixels = new int[width * height];
			byte[] buffer = new byte[srcBytesPerPixel];

			final boolean copyAlpha = (srcBytesPerPixel == 4) && acceptAlpha;
			final int dstBytesPerPixel = acceptAlpha ? srcBytesPerPixel : 3;
			final int trgLineSize = width * dstBytesPerPixel;

			int dstByteOffset = 0;

			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					int read = in.read(buffer, 0, srcBytesPerPixel);

					if (read < srcBytesPerPixel) {
						return pixels;
					}
					int actualByteOffset = dstByteOffset;
					if (!flipVertically) {
						actualByteOffset = ((height - y - 1) * trgLineSize)
								+ (x * dstBytesPerPixel);
					}

					if (copyAlpha) {
						writePixel(pixels, buffer[2], buffer[1], buffer[0],
								buffer[3], true, actualByteOffset);
					} else {
						writePixel(pixels, buffer[2], buffer[1], buffer[0],
								(byte) 0, false, actualByteOffset);
					}

					dstByteOffset += dstBytesPerPixel;
				}
			}
			return pixels;
		}

		private static void loadUncompressed(byte[] header, State tga,
				InputStream in, boolean acceptAlpha, boolean flipVertically)
				throws IOException {

			// 图像宽
			int orgWidth = getUnsignedShort(header, 12);

			// 图像高
			int orgHeight = getUnsignedShort(header, 14);

			// 图像位图(24&32)
			int pixelDepth = getUnsignedByte(header, 16);

			tga.width = orgWidth;
			tga.height = orgHeight;
			tga.pixelDepth = pixelDepth;

			boolean isOriginBottom = (header[17] & 0x20) == 0;

			if (!isOriginBottom) {
				flipVertically = !flipVertically;
			}

			// 不支持的格式
			if ((orgWidth <= 0) || (orgHeight <= 0)
					|| ((pixelDepth != 24) && (pixelDepth != 32))) {
				throw new IOException("Invalid texture information !");
			}

			int bytesPerPixel = (pixelDepth / 8);

			// 获取图像数据并转为int[]
			tga.pixels = readBuffer(in, orgWidth, orgHeight, bytesPerPixel,
					acceptAlpha, flipVertically);
			// 图像色彩模式
			tga.type = (acceptAlpha && (bytesPerPixel == 4) ? 4 : 3);
		}

		private static void loadCompressed(byte[] header, State tga,
				InputStream in, boolean acceptAlpha, boolean flipVertically)
				throws IOException {

			int orgWidth = getUnsignedShort(header, 12);
			int orgHeight = getUnsignedShort(header, 14);
			int pixelDepth = getUnsignedByte(header, 16);

			tga.width = orgWidth;
			tga.height = orgHeight;
			tga.pixelDepth = pixelDepth;

			boolean isOriginBottom = (header[17] & 0x20) == 0;

			if (!isOriginBottom) {
				flipVertically = !flipVertically;
			}

			if ((orgWidth <= 0) || (orgHeight <= 0)
					|| ((pixelDepth != 24) && (pixelDepth != 32))) {
				throw new IOException("Invalid texture information !");
			}

			int bytesPerPixel = (pixelDepth / 8);
			int pixelCount = orgHeight * orgWidth;
			int currentPixel = 0;

			byte[] colorBuffer = new byte[bytesPerPixel];

			int width = orgWidth;
			int height = orgHeight;

			final int dstBytesPerPixel = (acceptAlpha && (bytesPerPixel == 4) ? 4
					: 3);
			final int trgLineSize = orgWidth * dstBytesPerPixel;

			int[] pixels = new int[width * height];

			int dstByteOffset = 0;

			do {
				int chunkHeader = 0;
				try {
					chunkHeader = (byte) in.read() & 0xFF;
				} catch (IOException e) {
					throw new IOException(
							"Could not read RLE imageData header !");
				}

				boolean repeatColor;

				if (chunkHeader < 128) {
					chunkHeader++;
					repeatColor = false;
				} else {
					chunkHeader -= 127;
					readBuffer(in, colorBuffer);
					repeatColor = true;
				}

				for (int counter = 0; counter < chunkHeader; counter++) {
					if (!repeatColor) {
						readBuffer(in, colorBuffer);
					}

					int x = currentPixel % orgWidth;
					int y = currentPixel / orgWidth;

					int actualByteOffset = dstByteOffset;
					if (!flipVertically) {
						actualByteOffset = ((height - y - 1) * trgLineSize)
								+ (x * dstBytesPerPixel);
					}

					if (dstBytesPerPixel == 4) {
						writePixel(pixels, colorBuffer[2], colorBuffer[1],
								colorBuffer[0], colorBuffer[3], true,
								actualByteOffset);
					} else {
						writePixel(pixels, colorBuffer[2], colorBuffer[1],
								colorBuffer[0], (byte) 0, false,
								actualByteOffset);
					}

					dstByteOffset += dstBytesPerPixel;

					currentPixel++;

					if (currentPixel > pixelCount) {
						throw new IOException("Too many pixels read !");
					}
				}
			} while (currentPixel < pixelCount);

			tga.pixels = pixels;
			tga.type = dstBytesPerPixel;

		}

		public static State load(String res) throws IOException {
			return load(res, new State());
		}

		public static State load(String res, State tag) throws IOException {
			InputStream in = UIRes.getStream(res);
			State tga = load(in, tag, true, false);
			if (in != null) {
				try {
					in.close();
					in = null;
				} catch (Exception e) {
				}
			}
			return tga;
		}

		public static State load(InputStream in, State tga,
				boolean acceptAlpha, boolean flipVertically) throws IOException {
			if (in.available() < TGA_HEADER_SIZE) {
				return (null);
			}
			byte[] header = new byte[TGA_HEADER_SIZE];
			final int headerType = compareFormatHeader(in, header);
			if (headerType == TGA_HEADER_INVALID) {
				return (null);
			}
			if (headerType == TGA_HEADER_UNCOMPRESSED) {
				loadUncompressed(header, tga, in, acceptAlpha, flipVertically);
			} else if (headerType == TGA_HEADER_COMPRESSED) {
				loadCompressed(header, tga, in, acceptAlpha, flipVertically);
			} else {
				throw new IOException("State file be type 2 or type 10 !");
			}
			return tga;
		}
	}

	private final static String tgaExtension = ".tga";

	private final static ArrayList<LImage> images = new ArrayList<LImage>(100);

	private HashMap<Integer, LImage> subs;

	protected BufferedImage bufferedImage;

	private String fileName;

	private LGraphics g;

	private int width, height;

	private boolean isClose, isAutoDispose = true;

	public static LImage createImage(byte[] buffer) {
		return new LImage(GraphicsUtils.toolKit.createImage(buffer));
	}

	public static LImage createImage(byte[] buffer, int imageoffset,
			int imagelength) {
		return new LImage(GraphicsUtils.toolKit.createImage(buffer,
				imageoffset, imagelength));
	}

	public static LImage createImage(int width, int height) {
		return new LImage(width, height, false);
	}

	public static LImage createImage(int width, int height, boolean transparency) {
		return new LImage(width, height, transparency);
	}

	public static LImage createImage(int width, int height, int type) {
		return new LImage(width, height, type);
	}

	public static LImage createImage(String fileName) {
		return new LImage(fileName);
	}

	public static LImage createRGBImage(int[] rgb, int width, int height,
			boolean processAlpha) {
		if (rgb == null) {
			throw new NullPointerException();
		}
		if (width <= 0 || height <= 0) {
			throw new IllegalArgumentException();
		}
		BufferedImage img = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_ARGB);
		if (!processAlpha) {
			int l = rgb.length;
			int[] rgbAux = new int[l];
			for (int i = 0; i < l; i++) {
				rgbAux[i] = rgb[i] | 0xff000000;
			}
			rgb = rgbAux;
		}
		img.setRGB(0, 0, width, height, rgb, 0, width);
		return new LImage(img);
	}

	public static LImage[] createImage(int count, int w, int h,
			boolean transparency) {
		LImage[] image = new LImage[count];
		for (int i = 0; i < image.length; i++) {
			image[i] = new LImage(w, h, transparency);
		}
		return image;
	}

	public static LImage[] createImage(int count, int w, int h, int type) {
		LImage[] image = new LImage[count];
		for (int i = 0; i < image.length; i++) {
			image[i] = new LImage(w, h, type);
		}
		return image;
	}

	public static LImage createImage(LImage image, int x, int y, int width,
			int height, int transform) {
		int[] buf = new int[width * height];
		image.getPixels(buf, 0, width, x, y, width, height);
		int th;
		int tw;
		if ((transform & 4) != 0) {
			th = width;
			tw = height;
		} else {
			th = height;
			tw = width;
		}
		if (transform != 0) {
			int[] trans = new int[buf.length];
			int sp = 0;
			for (int sy = 0; sy < height; sy++) {
				int tx;
				int ty;
				int td;

				switch (transform) {
				case LGraphics.TRANS_ROT90:
					tx = tw - sy - 1;
					ty = 0;
					td = tw;
					break;
				case LGraphics.TRANS_ROT180:
					tx = tw - 1;
					ty = th - sy - 1;
					td = -1;
					break;
				case LGraphics.TRANS_ROT270:
					tx = sy;
					ty = th - 1;
					td = -tw;
					break;
				case LGraphics.TRANS_MIRROR:
					tx = tw - 1;
					ty = sy;
					td = -1;
					break;
				case LGraphics.TRANS_MIRROR_ROT90:
					tx = tw - sy - 1;
					ty = th - 1;
					td = -tw;
					break;
				case LGraphics.TRANS_MIRROR_ROT180:
					tx = 0;
					ty = th - sy - 1;
					td = 1;
					break;
				case LGraphics.TRANS_MIRROR_ROT270:
					tx = sy;
					ty = 0;
					td = tw;
					break;
				default:
					throw new RuntimeException("Illegal transformation: "
							+ transform);
				}

				int tp = ty * tw + tx;
				for (int sx = 0; sx < width; sx++) {
					trans[tp] = buf[sp++];
					tp += td;
				}
			}
			buf = trans;
		}

		return createRGBImage(buf, tw, th, true);
	}

	public LImage(int width, int height) {
		this(width, height, true);
	}

	public LImage(int width, int height, boolean transparency) {
		try {
			this.width = width;
			this.height = height;
			this.bufferedImage = GraphicsUtils.createImage(width, height,
					transparency);
		} catch (Exception e) {
			try {
				this.width = width;
				this.height = height;
				this.bufferedImage = GraphicsUtils.createImage(width, height,
						transparency);
			} catch (Exception ex) {
			}
		}
		if (!images.contains(this)) {
			images.add(this);
		}
	}

	public LImage(int width, int height, int type) {
		this.width = width;
		this.height = height;
		this.bufferedImage = GraphicsUtils.createImage(width, height, type);
		if (!images.contains(this)) {
			images.add(this);
		}
	}

	public LImage(String fileName) {
		if (fileName == null) {
			throw new RuntimeException("file name is null !");
		}
		String res;
		if (fileName.startsWith("/")) {
			res = fileName.substring(1);
		} else {
			res = fileName;
		}
		this.fileName = fileName;
		BufferedImage img = null;
		if (fileName.toLowerCase().lastIndexOf(tgaExtension) != -1) {
			try {
				LFormatTGA.State tga = LFormatTGA.load(res);
				if (tga != null) {
					img = GraphicsUtils.createImage(tga.width, tga.height,
							tga.type == 4 ? true : false);
					img.setRGB(0, 0, tga.width, tga.height, tga.pixels, 0,
							tga.width);
					tga.dispose();
					tga = null;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			img = GraphicsUtils.loadBufferedImage(res);
		}
		setImage(img);
		if (!images.contains(this)) {
			images.add(this);
		}
	}

	public LImage(BufferedImage img) {
		this.setImage(img);
		if (!images.contains(this)) {
			images.add(this);
		}
	}

	public LImage(Image img) {
		GraphicsUtils.waitImage(img);
		this.setImage(img);
		if (!images.contains(this)) {
			images.add(this);
		}
	}

	public void setImage(LImage img) {
		this.width = img.getWidth();
		this.height = img.getHeight();
		this.bufferedImage = img.bufferedImage;
		this.isAutoDispose = img.isAutoDispose;
	}

	public void setImage(BufferedImage img) {
		this.width = img.getWidth();
		this.height = img.getHeight();
		this.bufferedImage = img;
	}

	public void setImage(Image img) {
		this.width = img.getWidth(null);
		this.height = img.getHeight(null);
		this.bufferedImage = GraphicsUtils.getBufferImage(img);
	}

	public Object clone() {
		return new LImage(bufferedImage);
	}

	public ImageProducer getSource() {
		return bufferedImage.getSource();
	}

	public boolean hasAlpha() {
		return bufferedImage.getColorModel().hasAlpha();
	}

	public LGraphics getLGraphics() {
		if (g == null || g.isClose()) {
			g = new LGraphics(bufferedImage);
		}
		return g;
	}

	public LGraphics create() {
		return new LGraphics(bufferedImage);
	}

	public LImage getMirrorImage() {
		if (bufferedImage != null) {
			synchronized (bufferedImage) {
				LImage image = null;
				if (bufferedImage.getTransparency() == Transparency.TRANSLUCENT) {
					image = LImage.createImage(width, height, true);
				} else {
					image = LImage.createImage(width, height, false);
				}
				LGraphics g = image.getLGraphics();
				g.drawMirrorImage(bufferedImage, 0, 0);
				g.dispose();
				return image;
			}
		}
		return this;
	}

	public BufferedImage getBufferedImage() {
		return bufferedImage;
	}

	public int getWidth() {
		return bufferedImage.getWidth();
	}

	public int getHeight() {
		return bufferedImage.getHeight();
	}

	public Color getColorAt(int x, int y) {
		return new Color(this.getRGBAt(x, y), true);
	}

	public int getRGBAt(int x, int y) {
		if (x >= this.getWidth()) {
			throw new IndexOutOfBoundsException("X is out of bounds: " + x
					+ "," + this.getWidth());
		} else if (y >= this.getHeight()) {
			throw new IndexOutOfBoundsException("Y is out of bounds: " + y
					+ "," + this.getHeight());
		} else if (x < 0) {
			throw new IndexOutOfBoundsException("X is out of bounds: " + x);
		} else if (y < 0) {
			throw new IndexOutOfBoundsException("Y is out of bounds: " + y);
		} else {
			return this.bufferedImage.getRGB(x, y);
		}
	}

	public WritableRaster getRaster() {
		return bufferedImage.getRaster();
	}

	public int[] getPixels() {
		int pixels[] = new int[width * height];
		bufferedImage.getRGB(0, 0, width, height, pixels, 0, width);
		return pixels;
	}

	public int[] getPixels(int pixels[]) {
		bufferedImage.getRGB(0, 0, width, height, pixels, 0, width);
		return pixels;
	}

	public int[] getPixels(int x, int y, int w, int h) {
		int[] pixels = new int[w * h];
		bufferedImage.getRGB(x, y, w, h, pixels, 0, w);
		return pixels;
	}

	public int[] getPixels(int offset, int stride, int x, int y, int width,
			int height) {
		int pixels[] = new int[width * height];
		bufferedImage.getRGB(x, y, width, height, pixels, offset, stride);
		return pixels;
	}

	public int[] getPixels(int pixels[], int offset, int stride, int x, int y,
			int width, int height) {
		bufferedImage.getRGB(x, y, width, height, pixels, offset, stride);
		return pixels;
	}

	public void setPixels(int[] pixels, int width, int height) {
		bufferedImage.setRGB(0, 0, width, height, pixels, 0, width);
	}

	public void setPixels(int[] pixels, int offset, int stride, int x, int y,
			int width, int height) {
		bufferedImage.setRGB(x, y, width, height, pixels, offset, stride);
	}

	public int[] setPixels(int[] pixels, int x, int y, int w, int h) {
		bufferedImage.setRGB(x, y, w, h, pixels, 0, w);
		return pixels;
	}

	public void setPixel(Color c, int x, int y) {
		bufferedImage.setRGB(x, y, c.getRGB());
	}

	public void setPixel(int rgb, int x, int y) {
		bufferedImage.setRGB(x, y, rgb);
	}

	public int getPixel(int x, int y) {
		return bufferedImage.getRGB(x, y);
	}

	public int getRGB(int x, int y) {
		return bufferedImage.getRGB(x, y);
	}

	public void setRGB(int rgb, int x, int y) {
		bufferedImage.setRGB(x, y, rgb);
	}

	public LImage getCacheSubImage(int x, int y, int w, int h) {
		if (subs == null) {
			subs = new HashMap<Integer, LImage>(10);
		}
		int hashCode = 1;
		hashCode = LSystem.unite(hashCode, x);
		hashCode = LSystem.unite(hashCode, y);
		hashCode = LSystem.unite(hashCode, w);
		hashCode = LSystem.unite(hashCode, h);
		LImage img = (LImage) subs.get(hashCode);
		if (img == null) {
			subs.put(hashCode, img = new LImage(bufferedImage.getSubimage(x, y,
					w, h)));
		}
		return img;
	}

	public LImage getSubImage(int x, int y, int w, int h) {
		return new LImage(bufferedImage.getSubimage(x, y, w, h));
	}

	public LImage scaledInstance(int w, int h) {
		int width = getWidth();
		int height = getHeight();
		if (width == w && height == h) {
			return this;
		}
		return new LImage(GraphicsUtils.getResize(bufferedImage, w, h));
	}

	public void getRGB(int pixels[], int offset, int stride, int x, int y,
			int width, int height) {
		getPixels(pixels, offset, stride, x, y, width, height);
	}

	public int hashCode() {
		return GraphicsUtils.hashImage(bufferedImage);
	}

	public boolean isClose() {
		return isClose || bufferedImage == null;
	}

	public boolean isAutoDispose() {
		return isAutoDispose && !isClose();
	}

	public void setAutoDispose(boolean dispose) {
		this.isAutoDispose = dispose;
	}

	public String getPath() {
		return fileName;
	}

	public void dispose() {
		dispose(true);
	}

	private void dispose(boolean remove) {
		isClose = true;
		subs = null;
		if (bufferedImage != null) {
			bufferedImage.flush();
			bufferedImage = null;
		}
		if (remove) {
			images.remove(this);
		}
	}

	public static void disposeAll() {
		for (LImage img : images) {
			if (img != null) {
				img.dispose(false);
				img = null;
			}
		}
		images.clear();
	}
}
