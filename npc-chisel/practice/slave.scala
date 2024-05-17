class messege extends Bundle{
    val messege1 = Input(UInt(32.W))
}

class Slave extends Module{
    val io = IO(new Bundle{ val in = Decoupled(new messege)})

    val s_idle :: s_wait_valid :: Nil = Enum(2)
    val state = RegInit(s_idle)
    state := MuxLookup(state, s_idle)(List(
        s_idle       -> Mux(io.in.ready, s_wait_valid, s_idle),
        s_wait_valid -> Mux(io.in.valid, s_idle, s_wait_valid)
    ))
    io.in.ready := 已经准备好接受数据

    if state == s_idle then
        做本模块需要做的事情
        做完之后设置ready信号
    else if state == s_wait_valid then
        将属于本模块的所有时序逻辑元件的写/读使能全部置0
}