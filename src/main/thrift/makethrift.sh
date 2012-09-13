thrift -out ./src/ --gen java RequestThriftDTO.thrift 
thrift -out ./src/ --gen java JVMInfoThriftDTO.thrift 
thrift -out ./src/ --gen java RequestDataThriftDTO.thrift 
cp ./src/com/profiler/dto/*ThriftDTO.java /develop/eclipse_work_j2ee/TomcatProfilerReceiver/src/com/profiler/dto/
cp ./src/com/profiler/dto/*ThriftDTO.java /develop/eclipse_work_j2ee/TomcatProfilerDataFetch/src/com/profiler/dto/