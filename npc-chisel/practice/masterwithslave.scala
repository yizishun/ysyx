class messege1 extends Bundle{
    val messege1 = Input(UInt(32.W))
}
class messege2 extends Bundle{
    val messege2 = Output(UInt(32.W))
}

class MasterWithSlave extends Module{
    val io = new Bundle{
        val in = Decoupled(new messege1)
        val out = Decoupled(new messege2)
    }

    val in_ready = RegInit(io.in.ready)
    val out_valid = RegInit(io.out.valid)
    io.in.ready := in_ready
    io.out.valid := out_valid

    //如果本模块是IDU，Fire1的意思是IDU和IFU之间的ready和valid同时为1，Fire2即是IDU和EXU
    val s_BeforeFire1 :: s_BetweenFire12 :: Nil = Enum(2)
    val state = RegInit(s_BeforeFire1)
    val nextState = WireDefault(state)
    nextState := MuxLookup(state, s_BeforeFire1)(Seq(
        s_BeforeFire1   -> Mux(in.fire, s_BetweenFire12, s_BeforeFire1)
        s_BetweenFire12 -> Mux(out.fire, s_BeforeFire1, s_BetweenFire12)
    ))
    SetupXX()
    //default,it will error if no do this
    in_ready := false.B
    out_valid := false.B

    if nextState == s_BeforeFire1 then
        out_valid := 0.U
        in_ready := 1.U
        将属于本模块的所有时序逻辑元件的写/读使能全部置0
        以此保证本模块的状态不会改变
    else if nextState == s_BetweenFire12 then
        in_ready := 0.U
        先做本模块需要做的事情
        做完了之后设置valid信号(out_valid := 1.U)
        与此同时使用寄存器存贮本模块的输出(将寄存器的写使能置0 (置1的条件是out.fire == 1))
}
