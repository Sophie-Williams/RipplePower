package org.ripple.power.ui.projector.action.avg;

import org.ripple.power.config.LSystem;
import org.ripple.power.ui.graphics.LGraphics;
import org.ripple.power.ui.graphics.LImage;
import org.ripple.power.ui.projector.action.sprite.ISprite;
import org.ripple.power.ui.projector.core.LRelease;
import org.ripple.power.utils.StringUtils;

public class AVGChara implements LRelease {

	private LImage characterCG;

	private int width;

	private int height;

	int x;

	int y;

	int flag = -1;

	float time;

	float currentFrame;

	float opacity;

	protected boolean isMove, isVisible = true;

	int maxWidth, maxHeight;

	private int moveX;

	private int direction;

	private int moveSleep = 10;

	private boolean moving;

	/**
	 * 构造函数，初始化角色图
	 * 
	 * @param image
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	public AVGChara(LImage image, final int x, final int y, int width,
			int height) {
		this.load(image, x, y, width, height, LSystem.screenRect.width,
				LSystem.screenRect.height);
	}

	public AVGChara(LImage image, final int x, final int y) {
		this.load(image, x, y);
	}

	public AVGChara(final String resName, final int x, final int y) {
		this(resName, x, y, LSystem.screenRect.width, LSystem.screenRect.height);
	}

	public AVGChara(final String resName, final int x, final int y,
			final int w, final int h) {
		String path = resName;
		if (StringUtils.startsWith(path, '"')) {
			path = resName.replaceAll("\"", "");
		}
		this.load(LImage.createImage(path), x, y);
	}

	String tmp_path;

	void update(String path) {
		this.tmp_path = path;
	}

	private void load(LImage image, final int x, final int y) {
		this.load(image, x, y, image.getWidth(), image.getHeight(),
				LSystem.screenRect.width, LSystem.screenRect.height);
	}

	private void load(LImage image, final int x, final int y, int width,
			int height, final int w, final int h) {
		this.maxWidth = w;
		this.maxHeight = h;
		this.characterCG = image;
		this.isMove = true;
		this.width = width;
		this.height = height;
		this.x = x;
		this.y = y;
		this.moveX = 0;
		this.direction = getDirection();
		if (direction == 0) {
			this.moveX = -(width / 2);
		} else {
			this.moveX = maxWidth;
		}
	}

	public void setFlag(int f, float delay) {
		this.flag = f;
		this.time = delay;
		if (flag == ISprite.TYPE_FADE_IN) {
			this.currentFrame = this.time;
		} else {
			this.currentFrame = 0;
		}
	}

	public void finalize() {
		flush();
	}

	public int getScreenWidth() {
		return maxWidth;
	}

	public int getScreenHeight() {
		return maxHeight;
	}

	private int getDirection() {
		int offsetX = maxWidth / 2;
		if (x < offsetX) {
			return 0;
		} else {
			return 1;
		}
	}

	public void setMove(boolean move) {
		isMove = move;
	}

	public void flush() {
		characterCG = null;
		x = 0;
		y = 0;
	}

	public int getNext() {
		return moveX;
	}

	public int getMaxNext() {
		return x;
	}

	public boolean next() {
		moving = false;
		if (moveX != x) {
			for (int sleep = 0; sleep < moveSleep; sleep++) {
				if (direction == 0) {
					moving = (x > moveX);
				} else {
					moving = (x < moveX);
				}
				if (moving) {
					switch (direction) {
					case 0:
						moveX += 1;
						break;
					case 1:
						moveX -= 1;
						break;
					default:
						moveX = x;
						break;
					}
				} else {
					moveX = x;
				}
			}
		}
		return moving;
	}

	void update(long t) {

	}

	void draw(LGraphics g) {
		g.drawImage(characterCG, moveX, y);
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		if (isMove) {
			int move = x - this.moveX;
			if (move < 0) {
				this.moveX = this.x;
				this.x = x;
				direction = 1;
			} else {
				this.moveX = move;
				this.x = x;
			}
		} else {
			this.moveX = x;
			this.x = x;
		}

	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getMoveSleep() {
		return moveSleep;
	}

	public void setMoveSleep(int moveSleep) {
		this.moveSleep = moveSleep;
	}

	public int getMoveX() {
		return moveX;
	}
	
	public boolean isVisible() {
		return isVisible;
	}

	public void setVisible(boolean isVisible) {
		this.isVisible = isVisible;
	}

	public LImage getTexture() {
		return characterCG;
	}

	public void dispose() {
		this.isVisible = false;
		if (characterCG != null) {
			characterCG.dispose();
			characterCG = null;
		}
	}

}
