package com.github.hmdev.swing;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

import javax.swing.JPanel;

import com.github.hmdev.info.BookInfo;

/**
 * 表紙画像プレビューとトリミング用パネル
 */
public class JCoverImagePanel extends JPanel implements MouseListener, MouseMotionListener, DropTargetListener
{
	private static final long serialVersionUID = 1L;
	
	BookInfo bookInfo;
	private BufferedImage previewImage; 
	
	private double scale = -1;
	private int offsetX = 0;
	private int offsetY = 0;
	
	private int fitType = 0;
	final static int FIT_ALL = 0;
	final static int FIT_W = 1;
	final static int FIT_H = 2;
	final static int FIT_ZOOM = 3;
	
	private int startX = 0;
	private int startY = 0;
	
	public JCoverImagePanel()
	{
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
		new DropTarget(this, DnDConstants.ACTION_COPY_OR_MOVE, this, true);
	}
	
	/** パネル描画 */
	@Override
	public void paint(Graphics g)
	{
		super.paint(g);
		//g.clearRect(0, 0, this.getWidth(), this.getHeight());
		if (this.bookInfo != null && this.bookInfo.coverImage != null) {
			if (this.scale <= 0) this.setScale();
			if (this.previewImage == null) {
				this.createPreviewImage(this.scale);
			}
			if (this.previewImage != null) {
				int minX = this.getWidth()-this.previewImage.getWidth();
				int maxX = 0;
				int x = minX/2;
				if (minX < 0) x = Math.max(minX, Math.min(maxX, this.offsetX));
				this.offsetX = x;
				int minY = this.getHeight()-this.previewImage.getHeight();
				int maxY = 0;
				int y = minY/2;
				if (minY < 0) y = Math.max(minY,  Math.min(maxY, this.offsetY));
				else y = 0;
				this.offsetY = y;
				g.drawImage(this.previewImage, x, y, this);
			}
		}
	}
	
	/** プレビュー用の小さい画像を生成 */
	private void createPreviewImage(double scale)
	{
		BufferedImage previewImage = new BufferedImage((int)(this.bookInfo.coverImage.getWidth()*scale), (int)(this.bookInfo.coverImage.getHeight()*scale), BufferedImage.TYPE_INT_RGB);
		Graphics2D g2 = previewImage.createGraphics();
		try {
			AffineTransformOp ato = new AffineTransformOp(AffineTransform.getScaleInstance(scale, scale), AffineTransformOp.TYPE_BICUBIC);
			g2.drawImage(this.bookInfo.coverImage, ato, 0 ,0);
		} finally {
			g2.dispose();
		}
		this.previewImage = previewImage;
	}
	
	/** 画像ファイルを設定
	 * 画像入れ替え時もこれで行う
	 * プレビューはpaintで生成 */
	public void setBookInfo(BookInfo bookInfo)
	{
		this.bookInfo = bookInfo;
		this.previewImage = null;
		this.offsetX = 0;
		this.offsetY = 0;
		if (this.fitType == FIT_ZOOM) this.fitType = FIT_ALL;
		this.setScale();
		this.repaint();
	}
	/** 幅高さに合わせる */
	public void setFitType(int fitType)
	{
		this.fitType = fitType;
		this.previewImage = null;
		this.offsetX = 0;
		this.offsetY = 0;
		this.setScale();
		this.repaint();
	}
	/** 倍率変更 */
	public void setZoom(double zoom)
	{
		this.fitType = FIT_ZOOM;
		this.previewImage = null;
		this.scale *= zoom;
		this.offsetX *= zoom;
		this.offsetY *= zoom;
		this.repaint();
	}
	
	private void setScale()
	{
		if (bookInfo.coverImage == null) return;
		switch (this.fitType) {
		case FIT_ALL:
			this.scale = Math.min((double)this.getWidth()/bookInfo.coverImage.getWidth(), (double)this.getHeight()/this.bookInfo.coverImage.getHeight()); break;
		case FIT_W:
			this.scale = (double)this.getWidth()/bookInfo.coverImage.getWidth(); break;
		case FIT_H:
			this.scale = (double)this.getHeight()/this.bookInfo.coverImage.getHeight(); break;
		}
	}
	
	/** 画像範囲が変更されているか 表紙がなければfalse */
	public boolean isModified()
	{
		if (bookInfo.coverImage == null) return false;
		//トリミングなし
		double scale = Math.min((double)this.getWidth()/bookInfo.coverImage.getWidth(), (double)this.getHeight()/this.bookInfo.coverImage.getHeight());
		return this.scale != scale;
		
	}
	/** 編集された表紙を取得 */
	public BufferedImage getModifiedImage()
	{
		//表紙なし
		if (this.bookInfo.coverImage == null) return null;
		//トリミングなし
		double scale = Math.min((double)this.getWidth()/bookInfo.coverImage.getWidth(), (double)this.getHeight()/this.bookInfo.coverImage.getHeight());
		if (this.scale == scale) return null;
		
		//縮尺に合せてリサイズ 大きければ縮小
		int coverW = 600;
		int coverH = 800;
		double coverScale = (double)coverW/this.getWidth() * this.scale;
		coverW = (int)Math.min(coverW, bookInfo.coverImage.getWidth()*coverScale);
		coverH = (int)Math.min(coverH, this.bookInfo.coverImage.getHeight()*coverScale);
		if (coverScale > 1) {
			coverW /= coverScale;
			coverH /= coverScale;
			coverScale = 1;
		}
		
		int x = 0;
		if (this.getWidth() < this.previewImage.getWidth()) x = (int)(this.offsetX * (double)coverW/this.getWidth());
		int y = 0;
		if (this.getHeight() < this.previewImage.getHeight()) y = (int)(this.offsetY * (double)coverH/this.getHeight());
		BufferedImage coverImage = new BufferedImage(coverW, coverH, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2 = coverImage.createGraphics();
		try {
			//リサイズのatoとoffset指定でdrawImage
			AffineTransformOp ato = new AffineTransformOp(AffineTransform.getScaleInstance(coverScale, coverScale), AffineTransformOp.TYPE_BICUBIC);
			g2.setColor(Color.WHITE);
			g2.fillRect(0, 0, coverW, coverH);
			g2.drawImage(this.bookInfo.coverImage, ato, x ,y);
		} finally {
			g2.dispose();
		}
		return coverImage;
	}
	
	////////////////////////////////////////////////////////////////
	//Events
	////////////////////////////////////////////////////////////////
	@Override
	public void dragEnter(DropTargetDragEvent dtde) {}
	@Override
	public void dragOver(DropTargetDragEvent dtde) {}
	@Override
	public void dropActionChanged(DropTargetDragEvent dtde) {}
	@Override
	public void dragExit(DropTargetEvent dte) {}
	@Override
	public void drop(DropTargetDropEvent dtde)
	{
		dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
		Transferable transfer = dtde.getTransferable();
		try {
			DataFlavor[] flavars = transfer.getTransferDataFlavors();
			if (flavars.length == 0) return;
			if (flavars[0].isFlavorJavaFileListType()) {
				@SuppressWarnings("unchecked")
				List<File> files = (List<File>)transfer.getTransferData(DataFlavor.javaFileListFlavor);
				if (files.size() > 0) {
					bookInfo.coverFileName = files.get(0).getAbsolutePath();
					bookInfo.loadCoverImage(bookInfo.coverFileName);
					bookInfo.coverImageIndex = -1;
					repaint();
				}
			} else {
				for (DataFlavor flavar : flavars) {
					if (flavar.isFlavorTextType()) {
						bookInfo.coverFileName = transfer.getTransferData(DataFlavor.stringFlavor).toString();
						bookInfo.loadCoverImage(bookInfo.coverFileName);
						bookInfo.coverImageIndex = -1;
						repaint();
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
		}
	}
	
	@Override
	public void mouseClicked(MouseEvent e)
	{
	}
	@Override
	public void mouseEntered(MouseEvent e)
	{
	}
	@Override
	public void mouseExited(MouseEvent e)
	{
	}
	@Override
	public void mousePressed(MouseEvent e)
	{
		this.startX = e.getX();
		this.startX -= this.offsetX;
		this.startY = e.getY();
		if (this.offsetY != 0) this.startY -= this.offsetY;
	}
	@Override
	public void mouseReleased(MouseEvent e)
	{
		this.startX = Integer.MIN_VALUE;
	}
	/** マウスドラッグイベント */
	@Override
	public void mouseDragged(MouseEvent e)
	{
		if (this.startX != Integer.MIN_VALUE) {
			this.offsetX = e.getX()-this.startX;
			this.offsetY = e.getY()-this.startY;
			repaint();
		}
	}
	/** マウス移動イベント */
	@Override
	public void mouseMoved(MouseEvent e)
	{
	}
	
	
}