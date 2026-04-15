<script setup lang="ts">
import { useForm } from 'vee-validate'
import { toTypedSchema } from '@vee-validate/zod'
import { z } from 'zod'
import { useRouter } from 'vue-router'
import { authApi } from '@/api/auth.api'
import AuthLayout from '@/components/layouts/AuthLayout.vue'
import BaseInput from '@/components/ui/BaseInput.vue'
import BaseButton from '@/components/ui/BaseButton.vue'
import { useToast } from '@/composables/useToast'

const router = useRouter()
const { showToast } = useToast()

const schema = toTypedSchema(
  z.object({
    name: z.string().min(2, '이름은 2자 이상 입력해주세요'),
    email: z.string().email('유효한 이메일을 입력해주세요'),
    password: z.string().min(8, '비밀번호는 8자 이상 입력해주세요'),
    passwordConfirm: z.string()
  }).refine(data => data.password === data.passwordConfirm, {
    message: '비밀번호가 일치하지 않습니다',
    path: ['passwordConfirm']
  })
)

const { handleSubmit, isSubmitting, errors, defineField } = useForm({ validationSchema: schema })

const [name, nameAttrs] = defineField('name')
const [email, emailAttrs] = defineField('email')
const [password, passwordAttrs] = defineField('password')
const [passwordConfirm, passwordConfirmAttrs] = defineField('passwordConfirm')

const onSubmit = handleSubmit(async (values) => {
  try {
    await authApi.register({
      name: values.name,
      email: values.email,
      password: values.password
    })
    showToast('회원가입이 완료되었습니다. 로그인해주세요.', 'success')
    router.push({ name: 'login' })
  } catch (err: unknown) {
    const message =
      err instanceof Error ? err.message : '회원가입에 실패했습니다'
    showToast(message, 'error')
  }
})
</script>

<template>
  <AuthLayout>
    <div class="w-full max-w-md">
      <div class="text-center mb-8">
        <h1 class="text-3xl font-bold text-gray-900">회원가입</h1>
        <p class="mt-2 text-gray-600">계정을 만들어 서비스를 이용하세요</p>
      </div>

      <div class="card">
        <form @submit.prevent="onSubmit" class="space-y-4">
          <BaseInput
            v-model="name"
            v-bind="nameAttrs"
            label="이름"
            placeholder="홍길동"
            :error="errors.name"
          />
          <BaseInput
            v-model="email"
            v-bind="emailAttrs"
            label="이메일"
            type="email"
            placeholder="user@example.com"
            :error="errors.email"
          />
          <BaseInput
            v-model="password"
            v-bind="passwordAttrs"
            label="비밀번호"
            type="password"
            placeholder="8자 이상 입력"
            :error="errors.password"
          />
          <BaseInput
            v-model="passwordConfirm"
            v-bind="passwordConfirmAttrs"
            label="비밀번호 확인"
            type="password"
            placeholder="비밀번호 재입력"
            :error="errors.passwordConfirm"
          />

          <BaseButton
            type="submit"
            variant="primary"
            class="w-full"
            :loading="isSubmitting"
          >
            가입하기
          </BaseButton>
        </form>

        <p class="mt-6 text-center text-sm text-gray-600">
          이미 계정이 있으신가요?
          <RouterLink to="/login" class="font-medium text-primary-600 hover:text-primary-500">
            로그인
          </RouterLink>
        </p>
      </div>
    </div>
  </AuthLayout>
</template>
