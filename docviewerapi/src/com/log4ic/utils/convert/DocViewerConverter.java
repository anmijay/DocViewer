package com.log4ic.utils.convert;

import com.log4ic.DocViewer;
import com.log4ic.utils.io.FileUtils;
import com.log4ic.utils.convert.office.OfficeConverter;
import com.log4ic.utils.convert.pdf.PDFConverter;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.util.LinkedList;

/**
 * @author: 张立鑫
 * @date: 11-8-19 下午4:38
 */
public class DocViewerConverter {
    private static OfficeConverter officeConverter;
    private static PDFConverter pdfConverter;
    private static final Object lock = new Object();

    private static LinkedList<File> runningQueue = new LinkedList<File>();

    public static File deploy(File file, String outPath) {

        String fileName = FileUtils.getFilePrefix(file);

        File dir = new File(FileUtils.appendFileSeparator(outPath) + fileName);

        if (dir.isFile()) {
            dir.renameTo(new File(dir.getPath() + ".backup"));
        } else {
            dir.mkdirs();
        }

        return dir;
    }

    public static LinkedList<File> getRunningQueue() {
        return runningQueue;
    }

    /**
     * 转换为swf
     *
     * @param file
     * @param outPath
     * @return 返回转换后输出文件目录
     * @throws Exception
     */
    public static File toSwf(File file, String outPath) throws Exception {
        if (pdfConverter == null) {
            synchronized (lock) {
                if (pdfConverter == null) {
                    //PDFConverter.loadConfig();
                    pdfConverter = new PDFConverter();
                }
            }
        }
        try {
            synchronized (lock) {
                runningQueue.add(file);
            }
            String suffix = FileUtils.getFileSuffix(file);
            if (StringUtils.isBlank(suffix)) {
                throw new Exception("The file not has a suffix!");
            }
            File pdf = file;
            if (!suffix.toLowerCase().equals("pdf")) {
                pdf = toPDF(file, outPath);
            }

            return pdfConverter.convert(pdf, outPath, DocViewer.isSplitPage(), false);
        } finally {
            synchronized (lock) {
                runningQueue.remove(file);
            }
        }
    }

    public static File toPDF(File file, String outPath) throws Exception {
        if (officeConverter == null) {
            synchronized (lock) {
                if (officeConverter == null) {
                    officeConverter = new OfficeConverter();
                }
            }
        }
        try {
            synchronized (lock) {
                runningQueue.add(file);
            }
            File pdf = null;

            File dir = deploy(file, outPath);
            pdf = new File(FileUtils.appendFileSeparator(dir.getPath()) + FileUtils.getFilePrefix(file) + ".pdf");
            if (!pdf.exists()) {
                pdf = officeConverter.toPDF(file, dir.getPath());
            }
            return pdf;
        } finally {
            synchronized (lock) {
                runningQueue.remove(file);
            }
        }
    }
}
