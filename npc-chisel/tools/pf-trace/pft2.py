import sys
import pandas as pd # type: ignore
import matplotlib.pyplot as plt # type: ignore
import matplotlib.animation as animation # type: ignore
import os

trace1 = sys.argv[1]
trace2 = sys.argv[2]

# 创建一个空的图形框架
fig = plt.figure(figsize=(20, 8))

# 创建多个子图
ax1 = fig.add_subplot(221)
ax2 = fig.add_subplot(222)
ax3 = fig.add_subplot(223)
ax4 = fig.add_subplot(224)

def update(i):
    try:
        # 检查文件是否存在
        if not os.path.exists(trace1) or not os.path.exists(trace2):
            print("文件未找到，等待下一次更新...")
            return
        
        # 重新读取CSV文件
        df = pd.read_csv(trace1)
        df2 = pd.read_csv(trace2)
        
        # 检查必需的列是否存在
        required_columns1 = ['cpuCycle', '指令数', '跳转指令', '加载指令', '计算指令']
        required_columns2 = ['跳转指令占用时间', '加载指令占用时间', '存储指令占用时间', '计算指令占用时间', 'csr指令占用时间', '其他指令占用时间']
        
        if not all(col in df.columns for col in required_columns1) or not all(col in df2.columns for col in required_columns2):
            print("列未完全写入，等待下一次更新...")
            return

        # 清除上一次绘制内容
        ax1.clear()
        ax2.clear()
        ax3.clear()
        ax4.clear()

        # 绘制饼状图（时间）
        try:
            selected_columns = ['跳转指令占用时间', '加载指令占用时间', '存储指令占用时间', '计算指令占用时间', 'csr指令占用时间', '其他指令占用时间']
            if not df.empty:
                selected_values = df2[selected_columns].iloc[-1]
                ax1.pie(selected_values, labels=['jumpInst', 'loadInst', 'storeInst', 'calInst', 'csrInst', 'otherInst'], autopct='%1.1f%%', textprops={'fontsize': 4})
                ax1.set_title("Inst time")
            else:
                print("DataFrame is emplty")
        except:
            pass
        
        # 绘制折线图
        ax2.plot(df['cpuCycle'], df['指令数'], label='Inst')
        ax2.plot(df['cpuCycle'], df['跳转指令'], label='jumpInst')
        ax2.plot(df['cpuCycle'], df['加载指令'], label='loadInst')
        ax2.plot(df['cpuCycle'], df['计算指令'], label='calInst')
        ax2.set_title('Inst number trend')
        ax2.set_xlabel('cpuCycle')
        ax2.set_ylabel('number')
        ax2.legend()

        # 绘制饼状图（数量）
        selected_columns = ['IFU获得指令', 'EXU结束计算', 'LSU获得数据']
        selected_values = df[selected_columns].iloc[-1]
        ax3.pie(selected_values, labels=['IFUGetInst', 'EXUFinCal', 'LSUGetData'], autopct='%1.1f%%')
        ax3.set_title("3 Unit event number")

        selected_columns = ['跳转指令', '加载指令', '存储指令', '计算指令', 'csr指令', '其他指令']
        selected_values = df[selected_columns].iloc[-1]
        ax4.pie(selected_values, labels=['jumpInst', 'loadInst', 'storeInst', 'calInst', 'csrInst', 'otherInst'], autopct='%1.1f%%', textprops={'fontsize': 10})
        ax4.set_title("Inst number")

        # 更新整体图形的标题
        fig.suptitle('Performance Event Trace')
        plt.tight_layout()

    except Exception as e:
        print(f"发生异常: {e}")

# 调用animation.FuncAnimation来每隔1000毫秒调用一次update函数
ani = animation.FuncAnimation(fig, update, interval=0.1)

plt.show()
