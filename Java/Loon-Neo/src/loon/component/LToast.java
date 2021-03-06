package loon.component;

import loon.LSystem;
import loon.LTexture;
import loon.LTextures;
import loon.action.sprite.ISprite;
import loon.canvas.LColor;
import loon.component.skin.MessageSkin;
import loon.component.skin.SkinManager;
import loon.font.FontSet;
import loon.font.IFont;
import loon.opengl.GLEx;
import loon.utils.MathUtils;
import loon.utils.timer.LTimer;

/**
 * Example:
 * 
 * LToast toast = LToast.makeText(null, "ABCDEFG",Style.ERROR); add(toast);
 * 
 * or:
 * 
 * LToast.makeText("ABCDEFG",Style.ERROR).show();
 * 
 */
public class LToast extends LComponent implements FontSet<LToast> {

	public enum Style {
		NORMAL, SUCCESS, ERROR
	};

	public static LToast makeText(String text) {
		return makeText(SkinManager.get().getMessageSkin().getFont(), null,
				text, LENGTH_SHORT);
	}

	public static LToast makeText(String text, Style style) {
		return makeText(SkinManager.get().getMessageSkin().getFont(), null,
				text, LENGTH_SHORT, style);
	}

	public static LToast makeText(String text, int duration) {
		return makeText(SkinManager.get().getMessageSkin().getFont(), null,
				text, duration, Style.NORMAL);
	}

	public static LToast makeText(LComponent owner, String text) {
		return makeText(SkinManager.get().getMessageSkin().getFont(), owner,
				text, LENGTH_SHORT);
	}

	public static LToast makeText(LComponent owner, String text, Style style) {
		return makeText(SkinManager.get().getMessageSkin().getFont(), owner,
				text, LENGTH_SHORT, style);
	}

	public static LToast makeText(LComponent owner, String text, int duration) {
		return makeText(SkinManager.get().getMessageSkin().getFont(), owner,
				text, duration, Style.NORMAL);
	}

	public static LToast makeText(IFont font, LComponent owner, String text) {
		return makeText(font, owner, text, LENGTH_SHORT);
	}

	public static LToast makeText(IFont font, LComponent owner, String text,
			Style style) {
		return makeText(font, owner, text, LENGTH_SHORT, style);
	}

	public static LToast makeText(IFont font, LComponent owner, String text,
			int duration) {
		return makeText(font, owner, text, duration, Style.NORMAL);
	}

	public static LToast makeText(IFont font, LComponent owner, String text,
			int duration, Style style) {
		LToast toast = null;
		if (owner != null) {
			if (owner instanceof LToast) {
				return (LToast) owner;
			} else if (owner instanceof LContainer) {
				toast = new LToast(font, text, duration, owner.x(), owner.y(),
						(int) owner.getWidth(), (int) owner.getHeight());
				((LContainer) owner).add(toast);
			} else {
				toast = new LToast(font, text, duration, owner.x(), owner.y(),
						(int) owner.getWidth(), (int) owner.getHeight());
			}
		} else {
			toast = new LToast(font, text, duration, 0, 0,
					LSystem.viewSize.getWidth(), LSystem.viewSize.getHeight());
		}
		if (style == Style.SUCCESS) {
			toast.mBackgroundColor = SUCCESS_GRAY;
		}
		if (style == Style.ERROR) {
			toast.mBackgroundColor = ERROR_RED;
		}
		if (style == Style.NORMAL) {
			toast.mBackgroundColor = NORMAL_ORANGE;
		}
		return toast;
	}

	private boolean stop = false;
	public static final int LENGTH_SHORT = 30;
	public static final int LENGTH_LONG = 60;
	public static final LColor ERROR_RED = LColor.maroon;
	public static final LColor SUCCESS_GRAY = LColor.gray;
	public static final LColor NORMAL_ORANGE = LColor.orange.cpy();
	private final float MAX_OPACITY = 1.0f;
	private final float OPACITY_INCREMENT = 0.05f;
	private int _frame_radius = 15;
	private int _frame_length_multiplier = 10;
	private float opacity = 0;
	private String mText;
	private int mDuration;
	private LTimer timer = new LTimer();
	private LTimer lock = new LTimer(LSystem.SECOND * 2);
	private LColor mBackgroundColor = LColor.orange;
	private LColor mForegroundColor = LColor.white;
	private IFont font;
	private int displayX = 0;
	private int displayY = 0;
	private int cellHeight = 30;
	private int cellWidth = 30;
	private int mType;
	private boolean autoClose = true;

	public LToast(IFont font, String text, int duration, int x, int y,
			int width, int height) {
		this(font, SkinManager.get().getMessageSkin().getFontColor(), text,
				duration, x, y, width, height);
	}

	public LToast(IFont font, LColor fontColor, String text, int duration,
			int x, int y, int width, int height) {
		this(font, null, fontColor, text, duration, x, y, width, height);
	}

	public LToast(MessageSkin skin, String text, int duration, int x, int y,
			int width, int height) {
		this(skin.getFont(), skin.getBackgroundTexture(), skin.getFontColor(),
				text, duration, x, y, width, height);
	}

	public LToast(IFont font, LTexture backgounrd, LColor fontColor,
			String text, int duration, int x, int y, int width, int height) {
		super(x, y, width, height);
		this._drawBackground = false;
		this._background = backgounrd;
		this.mForegroundColor = fontColor;
		this.mType = ISprite.TYPE_FADE_IN;
		this.opacity = 0f;
		this.mDuration = duration;
		this.font = font;
		this.mText = text;
		this.cellWidth = font.stringWidth(mText)
				+ (_frame_length_multiplier * 10);
		this.cellHeight = font.getHeight() + 10;
		if (this.cellHeight < 30) {
			this.cellHeight = 30;
		}
		this.displayX = x + ((width / 2) - (cellWidth / 2));
		this.displayY = (y + ((height / 2) - (cellHeight / 2)))
				- font.getHeight() / 2;
		this.setSize(cellWidth, cellHeight);
		this.setLayer(1000);
		this.timer.setDelay(this.mDuration);
	}

	public void fadeIn() {
		this.mType = ISprite.TYPE_FADE_IN;
		this.opacity = 0f;
	}

	public void fadeOut() {
		this.mType = ISprite.TYPE_FADE_OUT;
		this.opacity = MAX_OPACITY;
	}

	@Override
	public void createUI(GLEx g, int x, int y, LComponent component,
			LTexture[] buttonImage) {
		if (!isVisible()) {
			return;
		}
		int w = (int) this.getWidth();
		int h = (int) this.getHeight();
		int oc = g.color();
		float alpha = g.alpha();
		try {
			g.setColor(mBackgroundColor);
			g.setAlpha(opacity);
			if (_background == null) {
				g.fillRoundRect(displayX, displayY, w, h, _frame_radius);
			} else {
				g.draw(_background, displayX, displayY, w, h, baseColor);
			}
			font.drawString(g, mText,
					displayX + (cellWidth - font.stringWidth(mText)) / 2,
					displayY + 2, mForegroundColor.cpy().setAlpha(getAlpha()));
		} finally {
			g.setColor(oc);
			g.setAlpha(alpha);
		}
	}

	@Override
	public void setBackground(LTexture texture) {
		this._background = texture;
	}

	@Override
	public void setBackground(String filePath) {
		setBackground(LTextures.loadTexture(filePath));
	}

	@Override
	public void update(long elapsedTime) {
		super.update(elapsedTime);
		if (timer.action(elapsedTime)) {
			if (mType == ISprite.TYPE_FADE_IN) {
				opacity += OPACITY_INCREMENT;
				opacity = (MathUtils.min(opacity, MAX_OPACITY));
				if (opacity >= MAX_OPACITY) {
					stop = true;
				}
			} else {
				opacity -= OPACITY_INCREMENT;
				opacity = (MathUtils.max(opacity, 0));
				if (opacity <= 0) {
					stop = true;
					setVisible(false);
					close();
					if (getScreen() != null) {
						getScreen().remove(this);
					}
				}
			}
		}
		if (stop && autoClose && lock.action(elapsedTime)) {
			fadeOut();
		}
	}

	public void setText(String text) {
		mText = text;
	}

	public void setDuration(int duration) {
		this.mDuration = duration;
		timer.setDelay(this.mDuration);
	}

	@Override
	public void setBackground(LColor backgroundColor) {
		super.setBackground(backgroundColor);
		mBackgroundColor = backgroundColor;
	}

	public void setForeground(LColor foregroundColor) {
		mForegroundColor = foregroundColor;
	}

	public boolean isStop() {
		return stop;
	}

	public boolean isAutoClose() {
		return autoClose;
	}

	public float getOpacity() {
		return opacity;
	}

	@Override
	public IFont getFont() {
		return font;
	}

	@Override
	public LToast setFont(IFont f) {
		this.font = f;
		return this;
	}

	public void setAutoClose(boolean autoClose) {
		this.autoClose = autoClose;
	}

	@Override
	public String getUIName() {
		return "Toast";
	}

}
