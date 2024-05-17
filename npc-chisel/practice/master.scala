class messege extends Bundle{
    val messege1 = Output(UInt(32.W))
}

class Master extends Module{
    val io = IO(new Bundle{ val out = Decoupled(new messege)})

    val s_idle :: s_wait_ready :: Nil = Enum(2)
    val state = RegInit(s_idle)
    state := MuxLookup(state, s_idle)(List(
        s_idle       -> Mux(io.out.valid, s_wait_ready, s_idle),
        s_wait_ready -> Mux(io.out.ready, s_idle, s_wait_ready)
    ))
    io.out.valid := 有指令需要发送
    
    if state == s_idle then
        做本模块需要做的事情
        做完了之后设置valid信号
    else if state == s_wait_ready then
        寄存器保存做完需要发送的结果(关闭寄存器的写使能)
        当fire时
        打开寄存器的写使能
}