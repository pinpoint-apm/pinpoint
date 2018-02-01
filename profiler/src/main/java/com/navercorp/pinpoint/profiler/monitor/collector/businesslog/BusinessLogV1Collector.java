package com.navercorp.pinpoint.profiler.monitor.collector.businesslog;

import static java.util.regex.Pattern.compile;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



import com.google.inject.Inject;
import com.navercorp.pinpoint.bootstrap.AgentDirBaseClassPathResolver;
import com.navercorp.pinpoint.bootstrap.BootstrapJarFile;
import com.navercorp.pinpoint.bootstrap.ClassPathResolver;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.common.util.Pair;
import com.navercorp.pinpoint.profiler.context.module.AgentId;
import com.navercorp.pinpoint.profiler.context.module.BootstrapJarPaths;
import com.navercorp.pinpoint.thrift.dto.TBusinessLogV1;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * [XINGUANG]Created by Administrator on 2017/7/21.
 */
public class BusinessLogV1Collector implements BusinessLogVXMetaCollector<TBusinessLogV1> {

    private final Logger logger = LoggerFactory.getLogger(BusinessLogV1Collector.class);

    private ProfilerConfig profilerConfig;
    private long nextLine;
    static final Pattern BUSINESS_LOG_PATTERN = compile("^BUSINESS_LOG_[A-Za-z0-9_-]*.log$");
    //static final String TIME_FIELD_PATTEN = "^[[1-9]\\\\d{3}\\\\-(0?[1-9]|1[0-2])\\\\-(0?[1-9]|[12]\\\\d|3[01])\\\\s*(0?[1-9]|1\\\\d|2[0-3])(\\\\:(0?[1-9]|[1-5]\\\\d)){2}]$";
    static final String TIME_FIELD_PATTEN = "^\\[([0-9]{3}[1-9]|[0-9]{2}[1-9][0-9]{1}|[0-9]{1}[1-9][0-9]{2}|[1-9][0-9]{3})-(((0[13578]|1[02])-(0[1-9]|[12][0-9]|3[01]))|((0[469]|11)-(0[1-9]|[12][0-9]|30))|(02-(0[1-9]|[1][0-9]|2[0-8]))) ([0-1]?[0-9]|2[0-3]):([0-5][0-9]):([0-5][0-9])\\]$";
    static final String THREAD_FIELD_PATTEN = "^[*]$";
    static final String LOG_LEVEL_FIELD_PATTEN = "^[a-zA-Z]{4,}";
    static final String CLASS_FIELD_PATTEN = "^[0-9a-zA-Z][.[0-9a-zA-Z]]*";
    static final String TXSPAN_BEGIN_FIELD_PATTEN = "^[*$";
    static final String TXSPAN_END_FIELD_PATTEN = "*]$";
    private List<String> businessLogList;
    private final static int linePerLogPerBatch = 1000;
    boolean lastLine = false;
    Long originLine = null;
    String[] nextLineContext = null;
    String agentId;
    String jarPath;
	int logIndex = 0;
    String agentPath;
    List<TBusinessLogV1> lastLogs = new ArrayList<TBusinessLogV1>();
    private HashMap<String, Pair<Date, Long>> dailyLogLineMap = new HashMap<String, Pair<Date, Long>>();

    private enum EnumField {
        TIME, THREAD, LOGLEVEL, CLASS, TXSPANBEGIN, TXSPANEND, MESSAGE
    }

    @Inject
    public BusinessLogV1Collector(ProfilerConfig profilerConfig, @AgentId String agentId) {
        this.profilerConfig = profilerConfig;
        this.nextLine = 0;
        businessLogList = new ArrayList<String>();
        this.agentId = agentId;
    }

    @Override
    public List<TBusinessLogV1> collect() {
        return getBusinessLogV1List();
    }

    @Override
    public void initDailyLogLineMap() {
        System.out.println("Init the dailyLogLineMap");
        //读文件，并初始化dailyLogLineMap
        agentPath = getAgentPath();
        String dirPath = agentPath + File.separator + "businessLogPersistence";
        String filePath = dirPath  + File.separator +  agentId + ".txt";
        File dir = new File(dirPath);
        if(!dir.exists()) {
            dir.mkdir();
            logger.info("创建存放记录读取日志持久化行数文件的文件夹");
        }
        File file = new File(filePath);
        if (file.exists()) {
            String encoding = "UTF-8";
            try {
                InputStreamReader isReader = new InputStreamReader(new FileInputStream(filePath), encoding);
                BufferedReader reader = new BufferedReader(isReader);
                try {
                    String lineTxt = reader.readLine();
                    while (lineTxt != null) {
                        String[] mapStr = lineTxt.split(",");
                        SimpleDateFormat sdf= new SimpleDateFormat("yyyy-MM-dd");
                        Date date = null;
                        try {
                            date = sdf.parse(mapStr[1]);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        Long line = Long.parseLong(mapStr[2]);
                        dailyLogLineMap.put(mapStr[0], new Pair<Date, Long>(date, line));
                        lineTxt = reader.readLine();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void saveLogMark() {
        //首先取到agent包的位置
        String dirPath = agentPath + File.separator + "businessLogPersistence";
        String filePath = dirPath   + File.separator +  agentId + ".txt";
        File dir = new File(dirPath);
        if (!dir.exists()) {
            dir.mkdir();
            logger.info("创建存放记录读取日志持久化行数文件的文件夹");
        }
        File file = new File(filePath);
        //写入内容
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }
        FileWriter fileWriter=null;
        try {
            fileWriter = new FileWriter(file);
        Iterator iter = dailyLogLineMap.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, Pair<Date, Long>> map = (Map.Entry<String, Pair<Date, Long>>) iter.next();
            StringBuilder str = new StringBuilder();
            str.append(map.getKey()).append(",");
            Date date = map.getValue().getKey();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String line = map.getValue().getValue().toString();
            str.append(sdf.format(date)).append(",").append(line).append("\r\n");
            fileWriter.write(str.toString());
            fileWriter.flush();
        }
    } catch (IOException e) {
        e.printStackTrace();
    } finally {
        try {
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    }

    private TBusinessLogV1 dealLastLog(String logPath, Long lineNum) {
        TBusinessLogV1 tBusinessLogV1 = new TBusinessLogV1();
        BufferedReader reader = generateBufferedReader(logPath);
        String dayTime = null;
        try {
            String lineTxt = reader.readLine();
            String[] lineTxts = lineTxt.split(" ");
            dayTime = lineTxts[0].split("\\[")[1];
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(checkTimeIsToday(dayTime)) {
            tBusinessLogV1 = getYesterdayLastLog(logPath, lineNum);
        }else {
            tBusinessLogV1 = getLastLog(logPath, lineNum);
        }

        return tBusinessLogV1;
    }

    private TBusinessLogV1 getYesterdayLastLog(String logPath, Long lineNum) {
        Date as = new Date(new Date().getTime()-24*60*60*1000);
        SimpleDateFormat matter1 = new SimpleDateFormat("yyyy-MM-dd");
        String yesterday = matter1.format(as);
        StringBuilder sb = new StringBuilder();
        String newLogPath = sb.append(logPath).append(".").append(yesterday).toString();
        return getLastLog(newLogPath, lineNum);
    }

    private TBusinessLogV1 getLastLog(String logPath, Long lineNum) {
        BufferedReader reader = generateBufferedReader(logPath);
        skipPreLines(reader, logPath, lineNum);
        String lineTxt;
        boolean firstLineInOneMessage = true;
        List<String[]> lineTxtsList = new ArrayList<String[]>();
        while (true) {
            try {
                if ((lineTxt = reader.readLine()) == null ) {
                    break;
                }
                String[] originLineTxts = lineTxt.split(" ");
                List<String> list = new ArrayList<String>();
                for (String txt : originLineTxts) {
                    if (!txt.isEmpty() && txt != null) {
                        list.add(txt.trim());
                    }
                }
                String lineTxts[] = new String[list.size()];
                for(int i=0,j=list.size();i<j;i++){
                    lineTxts[i]=list.get(i);
                }
                String time = "timestamp";

                if (lineTxts.length >= 2) {
                    time = lineTxts[0] + " " + lineTxts[1];

                }

                if(firstLineInOneMessage) {
                    if(time != null) {
                        lineTxts[0] = time;
                    }
                    firstLineInOneMessage = false;
                }
                lineTxtsList.add(lineTxts);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return generateTBusinessLogV1FromStringList(lineTxtsList);
    }


    private String getAgentPath() {
        final ClassPathResolver classPathResolver = new AgentDirBaseClassPathResolver();
        classPathResolver.verify();
        return classPathResolver.getAgentDirPath();
    }

    private File[] listFiles(final Pattern pattern, String logDirPath) {
        File logDir = new File(logDirPath);
        return logDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String fileName) {
                Matcher matcher = pattern.matcher(fileName);
                if (matcher.matches()) {
                	System.out.println(fileName);
                    return true;
                }
                return false;
            }
        });
    }

    boolean checkTimeIsToday(String dayTime) {
        Date now = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return dayTime.equals(simpleDateFormat.format(now));
    }

    boolean checkTimePatternMeet(String timeField) {
        if (timeField.matches(TIME_FIELD_PATTEN))
            return true;
        return false;
    }

    boolean checkThreadPatternMeet(String threadField) {
        if (threadField.matches(THREAD_FIELD_PATTEN))
            return true;
        return false;
    }

    boolean checkLogLevelMeet(String logLevelField) {
        if (logLevelField.matches(LOG_LEVEL_FIELD_PATTEN))
            return true;
        return false;
    }

    boolean checkClassFieldMeet(String classField) {
        if (classField.matches(CLASS_FIELD_PATTEN))
            return true;
        return false;
    }

    boolean checkTxSpanBeginMeet(String txSpanField) {
        if (txSpanField.matches(TXSPAN_BEGIN_FIELD_PATTEN))
            return true;
        return false;
    }

    boolean checkTxSpanEndMeet(String txSpanField) {
        if (txSpanField.matches(TXSPAN_END_FIELD_PATTEN))
            return false;
        return true;
    }

    List<String> generateFieldList(List<String[]> linesOfTxts) {
    	List<String> linesList = new ArrayList<String>();
    	for (String[] strArray : linesOfTxts) {
    		for (String str : strArray) {
    			linesList.add(str);
    		}
    	}
    	//删除多余的字符串
        if(linesList.size() < 10)
            return null;
        if(linesList.get(5) == null || linesList.get(5).trim() == "" ||linesList.size() < 2 )
            return null;
        String first = linesList.get(5).substring(0,1);
        String second = linesList.get(5).substring(1);

        if (!first.equals("[") || !second.equalsIgnoreCase("txid") || !linesList.get(8).equalsIgnoreCase("spanid"))
            return  null;

        linesList.remove(1);
        linesList.remove(4);
        linesList.remove(4);
        linesList.remove(5);
        linesList.remove(5);

       /* EnumField state = EnumField.TIME;
        for (String string : linesList) {
        	switch (state) {
            case TIME:
                if (checkTimePatternMeet(string) == false)
                    return null;
                stringList.add(string);
                state = EnumField.THREAD;
                break;
            case THREAD:
                if (checkThreadPatternMeet(string) == false)
                    return null;
                stringList.add(string);
                state = EnumField.LOGLEVEL;
                break;
            case LOGLEVEL:
                if (checkLogLevelMeet(string) == false)
                    return null;
                stringList.add(string);
                state = EnumField.CLASS;
                break;
            case CLASS:
                if (checkClassFieldMeet(string) == false)
                    return null;
                stringList.add(string);
                state = EnumField.TXSPANBEGIN;
                break;
            case TXSPANBEGIN:
                if (checkTxSpanBeginMeet(string) == false)
                    return null;
                txSpanBuilder.append(string);
                state = EnumField.TXSPANEND;
                break;
            case TXSPANEND:
                txSpanBuilder.append(string);
                if (checkTxSpanEndMeet(string) == true)
                    state = EnumField.MESSAGE;
                break;
            case MESSAGE:
                messageBuilder.append(string);
        	}
        }   
        if (state == EnumField.MESSAGE) {
            stringList.add(txSpanBuilder.toString());
            stringList.add(messageBuilder.toString());
        } else
            return null;*/
        return linesList;
    }

    Pair<String, String> retriveTractionIdFromField(String string) {
        String[] stringList = string.split(",");
        if (stringList.length != 2)
            return null;

        String[] subStringList1 = stringList[0].split(":");
        if (subStringList1.length != 2)
            return null;
        String txId = subStringList1[1].trim();

        String[] subStringList2 = stringList[1].split(":");
        if (subStringList2.length != 2)
            return null;
        String spanId = subStringList1[2].trim();
        return new Pair<String, String>(txId,spanId);
    }

    String retriveMessageFormField(List<String> fieldList, int index) {
        StringBuilder sb = new StringBuilder();
        for(int i = index; i < fieldList.size(); i++)
            sb.append(fieldList.get(i)).append(" ");
        return sb.toString();
    }

    TBusinessLogV1 assembleTBusinessLogV1(List<String> fieldList) {
        TBusinessLogV1 tBusinessLogV1 = new TBusinessLogV1();
        tBusinessLogV1.setTime(fieldList.get(0));
        tBusinessLogV1.setThreadName(fieldList.get(1));
        tBusinessLogV1.setLogLevel(fieldList.get(2));
        tBusinessLogV1.setClassName(fieldList.get(3));
        tBusinessLogV1.setTransactionId(fieldList.get(4));
        tBusinessLogV1.setSpanId(fieldList.get(5).split("]")[0]);
        tBusinessLogV1.setMessage(retriveMessageFormField(fieldList, 6));
        return tBusinessLogV1;
    }

    TBusinessLogV1 generateTBusinessLogV1FromStringList(List<String[]> linesOfTxts) {
        List<String> fieldList = generateFieldList(linesOfTxts);
        if (fieldList == null)
            return null;
        return assembleTBusinessLogV1(fieldList);
    }

    private Pair<Long, TBusinessLogV1> readOneLogFromLineInner(BufferedReader reader, String businessLogV1, Long line) {
        boolean assemblyData = false;
        Long nextLine = line;
        String lineTxt;
        boolean firstLineInOneMessage = true;
        List<String[]> linesOfTxts = new ArrayList<String[]>();
        while (true) {
        	if (nextLineContext != null) {
        		linesOfTxts.add(nextLineContext);
        		nextLineContext = null;
        		firstLineInOneMessage = false;
        	} else {        	
	            try {
	                if ((lineTxt = reader.readLine()) == null ) {
	                	lastLine = true;
	                	break;
	                }
                    nextLine++;
	                if(nextLine - originLine > 120) {
	                    break;
                    }
	                if("".equals(lineTxt)){
	                    continue;
                    }

	                String[] originLineTxts = lineTxt.split(" ");
                    List<String> list = new ArrayList<String>();
                    for (String txt : originLineTxts) {
                        if (!txt.isEmpty() && txt != null) {
                            list.add(txt.trim());
                        }
                    }
                    String lineTxts[] = new String[list.size()];
                    for(int i=0,j=list.size();i<j;i++){
                        lineTxts[i]=list.get(i);
                    }
                    String time = "timestamp";

                    if (lineTxts.length >= 2) {
                        time = lineTxts[0] + " " + lineTxts[1];

                    }

	                if (firstLineInOneMessage) {                 	
	                    if (!checkTimePatternMeet(time)) {
	                        //corrupt log format
	                        return new Pair(nextLine, null);
	                    }
	                    String dayTime = lineTxts[0].split("\\[")[1];
	                    //因为rollingday的日志文件只有在第二天的日志生成的时候才会重新刷新，所以需要检查日志是否为当天的日志
                        if (!checkTimeIsToday(dayTime)) {
                            return new Pair(nextLine, null);
                        }
                        if(time != null) {
                            lineTxts[0] = time;
                        }
	                    firstLineInOneMessage = false;
	                } else {
	                    if (checkTimePatternMeet(time)) {
                            if(time != null) {
                                lineTxts[0] = time;
                            }
	                    	nextLineContext = lineTxts;
	                    	Long markLine = nextLine;

                            Date newdate = dailyLogLineMap.get(businessLogV1).getKey();
                            dailyLogLineMap.put(businessLogV1, new Pair<Date, Long>(newdate, (markLine - 1)));
                            assemblyData = true;
	                    	break;
	                    }
	                        
	                }
	                linesOfTxts.add(lineTxts);   
	            } catch (IOException e1) {
	                e1.printStackTrace();
	                return new Pair<Long, TBusinessLogV1>(line, null);
	            }
        	}
            
        }
        TBusinessLogV1 tBusinessLogV1 = new TBusinessLogV1();
        if (linesOfTxts.size() != 0 && assemblyData == true) {
            tBusinessLogV1 = generateTBusinessLogV1FromStringList(linesOfTxts);
        }
        if (tBusinessLogV1 == null)
            return new Pair<Long, TBusinessLogV1>(nextLine, null);
        return new Pair<Long, TBusinessLogV1>(nextLine, tBusinessLogV1);
    }

    boolean skipPreLines(BufferedReader reader, String businessLogV1, Long line) {
        Long initValue = 0L;
        String lineTxt;
        while (line.compareTo(initValue) > 0) {
            try {
                if ((lineTxt = reader.readLine()) == null) {
                    return false;
                }
            } catch (IOException e1) {
                e1.printStackTrace();
                return false;
            }
            initValue++;
        }
        return true;
    }

    private Pair<Long, TBusinessLogV1> readOneLogFromLine(BufferedReader reader, String businessLogV1, Long line) {

        return readOneLogFromLineInner(reader, businessLogV1, line);
    }

    private BufferedReader generateBufferedReader(String businessLogV1) {
        String encoding = "UTF-8";
        try {
            InputStreamReader isReader = new InputStreamReader(new FileInputStream(businessLogV1), encoding);
            BufferedReader reader = new BufferedReader(isReader);
            return reader;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }

    }

    private List<TBusinessLogV1> readLogFromBusinessLog(String businessLogV1) {
        List<TBusinessLogV1> tBusinessLogV1List = new ArrayList<TBusinessLogV1>();
        //[XINGUANG] generate reader for businessLogV1 file
        BufferedReader reader = generateBufferedReader(businessLogV1);
        if (reader == null)
            return tBusinessLogV1List;
        //[XINGUANG] get line number
        Pair<Date, Long> dateLinePair = dailyLogLineMap.get(businessLogV1);
        Long line = dateLinePair.getValue();
        Date date = dateLinePair.getKey();
        originLine = line;
        
        //[XINGUANG] skip prelines
        boolean flag = skipPreLines(reader, businessLogV1, line);
        for (int i = 0; i < linePerLogPerBatch; i++) {
            Pair<Long, TBusinessLogV1> tBusinessLogV1LinePair = readOneLogFromLine(reader, businessLogV1, line);
            if (tBusinessLogV1LinePair.getKey() != line) {
                if(tBusinessLogV1LinePair.getKey() - originLine >= 120) {
                    break;
                }
                line = tBusinessLogV1LinePair.getKey();

                if (tBusinessLogV1LinePair.getValue() != null) {
                    tBusinessLogV1List.add(tBusinessLogV1LinePair.getValue());
                } else {
                    tBusinessLogV1List.add(new TBusinessLogV1());
                }

            } else {
                break;
            }
        }
        return tBusinessLogV1List;
    }

    private List<TBusinessLogV1> readLogFromBusinessLogList() {
        List<TBusinessLogV1> tBusinessLogV1List = new ArrayList<TBusinessLogV1>();
        if (!businessLogList.isEmpty()) {
            int num = businessLogList.size();
            String businessLogV1 = businessLogList.get(logIndex % num);
            logIndex++;
            if(logIndex == num) {
                logIndex = 0;
            }
            //for (String businessLogV1 : businessLogList) {

                if (dailyLogLineMap.containsKey(businessLogV1)) {
                    List<TBusinessLogV1> tBusinessLogV1ListSub = readLogFromBusinessLog(businessLogV1);
                    //[XINGUANG] gurantee the order by first added list ,second added list ... when resolve data from tBusinessLogV1List?
                    tBusinessLogV1List.addAll(tBusinessLogV1ListSub);
                }
            //}
        }
        if (!lastLogs.isEmpty()) {
            tBusinessLogV1List.addAll(lastLogs);
            lastLogs.clear();
        }
        return tBusinessLogV1List;
    }

    private void generateLogLineMap() {
        if (businessLogList != null && !businessLogList.isEmpty()) {
            for (String businessLogV1 : businessLogList) {
                Pair<Date, Long> dailyLine = dailyLogLineMap.get(businessLogV1);
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                if (dailyLine == null) {
                    Date date;
                    try {
                        date = df.parse(df.format(new Date()));
                        dailyLogLineMap.put(businessLogV1, new Pair<Date, Long>(date, 0l));
                    } catch (ParseException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                } else {
                    Date originDate = dailyLine.getKey();
                    Date date;
                    try {
                        date = df.parse(df.format(new Date()));
                        if (originDate.before(date)) {
                            TBusinessLogV1 lastLog = dealLastLog(businessLogV1, dailyLogLineMap.get(businessLogV1).getValue());
                            lastLogs.add(lastLog);
                            dailyLogLineMap.put(businessLogV1, new Pair<Date, Long>(date, 0l));
                        }
                    } catch (ParseException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }
    }
    
    private void generateBusinessLogList(List<String> filedirList) {
        for (String filedir : filedirList) {
            File file = new File(filedir);
            if (file.exists() && file.isFile()) {
                businessLogList.add(file.toString());
            } else {
                logger.error("business log dir:" + filedir + " is not exist.");
            }
        }
    }

    private List<String> getCorrespondLogDir(String tomcatLogDirs) {
        String[] tomcatLogDirList = tomcatLogDirs.split(";");
        HashMap<String, List<String>> agentIdLogDirMap = new HashMap<String, List<String>>();
        if (tomcatLogDirList != null && tomcatLogDirList.length != 0) {
            for (String tomcatLogDir : tomcatLogDirList) {
                logger.info("agentIdAndLog is " + tomcatLogDir);
                String[] agentIdAndLog = tomcatLogDir.split("=");
                //agentId和logPath中不能带“=”，且两边都要存在
                if (agentIdAndLog.length == 2) {
                    List<String> logDirList = new ArrayList<String>();
                    if(agentIdLogDirMap.get(agentIdAndLog[0].trim()) != null) {
                        logDirList = agentIdLogDirMap.get(agentIdAndLog[0].trim());
                    }
                    if(!logDirList.contains(agentIdAndLog[1].trim())) {
                        logDirList.add(agentIdAndLog[1].trim());
                    }
                    agentIdLogDirMap.put(agentIdAndLog[0].trim(), logDirList);
                } else {
                    logger.error("profiler.tomcatlog.dir is wrong.agentIdAndLog is " + tomcatLogDir);
                }
            }
        } else {
            logger.error("profiler.tomcatlog.dir is null or empty.");
            return null;
        }
        List<String> agentLogDirList = agentIdLogDirMap.get(agentId);
        if (agentLogDirList != null) {
            return agentLogDirList;
        } else {
            return null;
        }
    }

    private List<TBusinessLogV1> getBusinessLogV1List() {
        try {
            String tomcatLogDirs = profilerConfig.getTomcatLogDir();
            businessLogList.clear();
            List<String> tomcatLogDir = getCorrespondLogDir(tomcatLogDirs);
            if (tomcatLogDir != null) {
                //File[] files = listFiles(BUSINESS_LOG_PATTERN, tomcatLogDir.trim());
                generateBusinessLogList(tomcatLogDir);

                generateLogLineMap();
                //[XINGUANG] read BusinessLogList
                return readLogFromBusinessLogList();
            } else {
                logger.error("This application does not have a configuration business log directory");
                return new ArrayList<TBusinessLogV1>();
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<TBusinessLogV1>();
    }
}